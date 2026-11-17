UPDATE af.wb_list_candidate AS candidate
SET list_name = 'FINGERPRINT'
WHERE candidate.list_name = 'FINGERPRRINT'
  AND candidate.approved = false
  AND NOT EXISTS (SELECT 1
                  FROM af.wb_list_candidate AS existing
                  WHERE existing.list_name = 'FINGERPRINT'
                    AND existing.value = candidate.value);
