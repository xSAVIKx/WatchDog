/**
 * watchdog website
 * select websites to check
 * last check was longer ago than the set interval in minutes
 */
SELECT
"watchdog_website" AS source,
website_id AS reference_id,
website_url AS URL_to_check
FROM watchdog_website AS watch
WHERE ( lastverified + INTERVAL frequency MINUTE) < UTC_TIMESTAMP()
OR lastverified is null
ORDER BY lastverified asc
LIMIT 100
;

/**
 * watchdog website
 * update done
 */
UPDATE watchdog_website
SET lastverified = UTC_TIMESTAMP()
WHERE website_id = 'id'
;

/**
 * watchdog website
 * websites log, get last entry
 */
SELECT
id,
return_status_code,
return_status_text
FROM watchdog_website_log
WHERE website_id = 999
ORDER BY last_encountered DESC
LIMIT 1
;

/**
 * watchdog website
 * Same result, update last_encountered
 */
UPDATE watchdog_website_log
SET last_encountered = UTC_TIMESTAMP()
WHERE id  = 'id';

/**
 * watchdog website
 * Different result, new entry
 */
INSERT INTO watchdog_website_log
(id,website_id,first_encountered,last_encountered,return_status_code,return_status_text) 
VALUES(null, 'id' ,UTC_TIMESTAMP(),UTC_TIMESTAMP(), 'code', 'text')
;






/**
 * watchdog timestamp
 * select websites to check
 * last check was longer ago than the set interval in minutes
 */
SELECT
"watchdog_timestamp" AS source,
website_id AS reference_id,
website_url AS URL_to_check,
maximum_age
FROM watchdog_timestamp AS watch
WHERE ( lastverified + INTERVAL frequency MINUTE) < UTC_TIMESTAMP()
OR lastverified is null
ORDER BY lastverified asc
LIMIT 100
;

/**
 * watchdog timestamp
 * websites log, get last entry
 */
SELECT
id,
return_status_code,
return_status_text,
timestamp_found
FROM watchdog_timestamp_log
WHERE website_id = 999
ORDER BY last_encountered DESC
LIMIT 1
;

/**
 * watchdog timestamp
 * update done
 */
UPDATE watchdog_timestamp
SET lastverified = UTC_TIMESTAMP()
WHERE website_id = 'id'
;

/**
 * watchdog timestamp
 * Same result, update last_encountered
 */
UPDATE watchdog_timestamp_log
SET last_encountered = UTC_TIMESTAMP()
WHERE id  = 'id';

/**
 * watchdog timestamp
 * Different result, new entry
 */
INSERT INTO watchdog_timestamp_log
(id,website_id,first_encountered,last_encountered,return_status_code,return_status_text,timestamp_found) 
VALUES(null, 'id' ,UTC_TIMESTAMP(),UTC_TIMESTAMP(), 'code', 'text','true')
;
