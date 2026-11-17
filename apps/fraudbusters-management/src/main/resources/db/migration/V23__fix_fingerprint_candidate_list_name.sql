-- Fingerprint candidates were created in the misspelled list FINGERPRRINT, which fraudbusters never reads
-- (inBlackList("fingerprint") looks up FINGERPRINT). Pending candidates move to the correct list so that their
-- approval creates a usable row. Approved candidates keep their name: their rows already live in wb-list-manager
-- under FINGERPRRINT and are moved there separately. A pending candidate whose value is already in FINGERPRINT
-- stays as it is to keep (list_name, value) unique.
UPDATE af.wb_list_candidate AS candidate
SET list_name = 'FINGERPRINT'
WHERE candidate.list_name = 'FINGERPRRINT'
  AND candidate.approved = false
  AND NOT EXISTS (SELECT 1
                  FROM af.wb_list_candidate AS existing
                  WHERE existing.list_name = 'FINGERPRINT'
                    AND existing.value = candidate.value);
