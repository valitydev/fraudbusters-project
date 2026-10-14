package dev.vality.fraudbusters.notificator.dao;

import dev.vality.dao.impl.AbstractGenericDao;

import javax.sql.DataSource;

public abstract class AbstractDao extends AbstractGenericDao {

    public AbstractDao(DataSource dataSource) {
        super(dataSource);
    }

}
