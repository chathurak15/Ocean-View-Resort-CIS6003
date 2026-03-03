-- Stored procedure, cancel a reservation by reservation number
-- Enforces business rule, cannot cancel an already cancelled reservation

DELIMITER $$

CREATE PROCEDURE cancel_reservation(IN p_res_no VARCHAR(50))
BEGIN
    DECLARE v_status VARCHAR(20);

    SELECT status INTO v_status
    FROM reservations
    WHERE reservation_no = p_res_no;

    -- reservation not found
    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Reservation not found';
    END IF;

    -- already cancelled
    IF v_status = 'CANCELLED' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Reservation is already cancelled';
    END IF;

    UPDATE reservations
    SET status = 'CANCELLED'
    WHERE reservation_no = p_res_no;
END$$

DELIMITER ;
