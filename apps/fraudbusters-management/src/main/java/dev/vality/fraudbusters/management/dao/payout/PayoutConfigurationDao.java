package dev.vality.fraudbusters.management.dao.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.wb_list.Event;
import dev.vality.damsel.wb_list.EventType;
import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.utils.RowUtilsService;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

/** Durable projection of the existing command stream, isolated from payment administration. */
@Repository
public class PayoutConfigurationDao extends AbstractDao {

    private final RowUtilsService rowUtilsService;

    public PayoutConfigurationDao(DataSource dataSource, RowUtilsService rowUtilsService) {
        super(dataSource);
        this.rowUtilsService = rowUtilsService;
    }

    public static boolean supports(Command command) {
        if (command == null || !command.isSetCommandBody()) {
            return false;
        }
        var body = command.getCommandBody();
        return body.isSetTemplate() && body.getTemplate().getOperationType() == OperationType.payout
                || body.isSetGroup() && body.getGroup().getOperationType() == OperationType.payout
                || body.isSetReference() && body.getReference().getOperationType() == OperationType.payout
                || body.isSetGroupReference() && body.getGroupReference().getOperationType() == OperationType.payout;
    }

    @Transactional
    public boolean apply(Command command) {
        var body = command.getCommandBody();
        if (command.getCommandType() == null) {
            throw new IllegalArgumentException("command_type is required");
        }
        boolean create = command.getCommandType() == CommandType.CREATE;
        if (body.isSetReference() && body.getReference().getOperationType() != OperationType.payout
                && body.getReference().isSetWalletId()
                || body.isSetGroupReference() && body.getGroupReference().getOperationType() != OperationType.payout
                && body.getGroupReference().isSetWalletId()) {
            throw new IllegalArgumentException("wallet_id is not supported for payments");
        }
        if (body.isSetTemplate() && body.getTemplate().getOperationType() == OperationType.payout) {
            var template = body.getTemplate();
            if (create) {
                query("""
                        INSERT INTO af.payout_template(id, template) VALUES (?, ?)
                        ON CONFLICT (id) DO UPDATE SET template = EXCLUDED.template
                        """, template.getId(), new String(template.getTemplate(), StandardCharsets.UTF_8));
            } else {
                query("DELETE FROM af.payout_template WHERE id = ?", template.getId());
            }
        } else if (body.isSetGroup() && body.getGroup().getOperationType() == OperationType.payout) {
            var group = body.getGroup();
            query("DELETE FROM af.payout_group WHERE group_id = ?", group.getGroupId());
            if (create) {
                for (var priority : group.getTemplateIds()) {
                    query("INSERT INTO af.payout_group(group_id, template_id, priority) VALUES (?, ?, ?)",
                            group.getGroupId(), priority.getId(), priority.getPriority());
                }
            }
        } else if (body.isSetReference() && body.getReference().getOperationType() == OperationType.payout) {
            var reference = body.getReference();
            boolean global = reference.isIsGlobal();
            String party = global ? "" : identity(reference.getPartyId());
            String wallet = global ? "" : identity(reference.getWalletId());
            validateScope(global ? null : reference.getShopId(), party, wallet);
            if (create) {
                query("""
                        INSERT INTO af.payout_reference(party_id, wallet_id, template_id, is_global)
                        VALUES (?, ?, ?, ?) ON CONFLICT (party_id, wallet_id, is_global)
                        DO UPDATE SET template_id = EXCLUDED.template_id
                        """, party, wallet, reference.getTemplateId(), global);
            } else {
                query("DELETE FROM af.payout_reference WHERE party_id = ? AND wallet_id = ? AND is_global = ?",
                        party, wallet, global);
            }
        } else if (body.isSetGroupReference()
                && body.getGroupReference().getOperationType() == OperationType.payout) {
            var reference = body.getGroupReference();
            String party = identity(reference.getPartyId());
            String wallet = identity(reference.getWalletId());
            validateScope(reference.getShopId(), party, wallet);
            if (create) {
                query("""
                        INSERT INTO af.payout_group_reference(party_id, wallet_id, group_id) VALUES (?, ?, ?)
                        ON CONFLICT (party_id, wallet_id) DO UPDATE SET group_id = EXCLUDED.group_id
                        """, party, wallet, reference.getGroupId());
            } else {
                query("DELETE FROM af.payout_group_reference WHERE party_id = ? AND wallet_id = ?", party, wallet);
            }
        } else {
            return false;
        }
        return true;
    }

    @Transactional
    public void applyList(Event event) {
        var row = event.getRow();
        var id = row.getId().getPayoutId();
        String party = identity(id.getPartyId());
        String wallet = identity(id.getWalletId());
        validateScope(null, party, wallet);
        if (event.getEventType() == EventType.CREATED) {
            query("""
                    INSERT INTO af.payout_list_record(party_id, wallet_id, list_type, list_name, value, row_info)
                    VALUES (?, ?, ?::af.list_type, ?, ?, ?)
                    ON CONFLICT (party_id, wallet_id, list_type, list_name, value)
                    DO UPDATE SET row_info = EXCLUDED.row_info
                    """, party, wallet, row.getListType().name(), row.getListName(), row.getValue(),
                    rowUtilsService.initRowInfo(row));
        } else if (event.getEventType() == EventType.DELETED) {
            query("""
                    DELETE FROM af.payout_list_record WHERE party_id = ? AND wallet_id = ?
                    AND list_type = ?::af.list_type AND list_name = ? AND value = ?
                    """, party, wallet, row.getListType().name(), row.getListName(), row.getValue());
        } else {
            throw new IllegalArgumentException("Unsupported payout list event");
        }
    }

    private void query(String sql, Object... values) {
        execute(getDslContext().query(sql, values));
    }

    private String identity(String value) {
        return value == null ? "" : value;
    }

    private void validateScope(String shop, String party, String wallet) {
        if (StringUtils.hasText(shop) || StringUtils.hasText(wallet) && !StringUtils.hasText(party)) {
            throw new IllegalArgumentException("Invalid payout scope");
        }
    }
}
