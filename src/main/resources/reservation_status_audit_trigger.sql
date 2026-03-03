-- Trigger (log reservation status changes to reservation_audit table)
-- Fires AFTER any UPDATE on the reservations table
-- Requires (reservation_audit table (see schema below))

-- create audit table if not exists
CREATE TABLE IF NOT EXISTS reservation_audit (
    audit_id     INT AUTO_INCREMENT PRIMARY KEY,
    reservation_no VARCHAR(50) NOT NULL,
    old_status   VARCHAR(20),
    new_status   VARCHAR(20),
    changed_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$

CREATE TRIGGER trg_reservation_status_audit
AFTER UPDATE ON reservations
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO reservation_audit (reservation_no, old_status, new_status, changed_at)
        VALUES (NEW.reservation_no, OLD.status, NEW.status, NOW());
    END IF;
END$$

DELIMITER ;
