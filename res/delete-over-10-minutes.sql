DELETE FROM DataCollectorDev.Move WHERE UNIX_TIMESTAMP() * 1000 - 10 * 60 * 1000 > Date;