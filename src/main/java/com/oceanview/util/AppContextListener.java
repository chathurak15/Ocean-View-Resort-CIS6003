package com.oceanview.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("OceanView System Starting...");

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("OceanView Sorry, unable to find db.properties");
                return;
            }

            Properties prop = new Properties();
            prop.load(input);

            // Set them as System Properties
            System.setProperty("DB_DRIVER", prop.getProperty("db.driver"));
            System.setProperty("DB_URL", prop.getProperty("db.url"));
            System.setProperty("DB_USER", prop.getProperty("db.user"));
            System.setProperty("DB_PASSWORD", prop.getProperty("db.password"));

            System.out.println("OceanView Configuration Loaded Successfully.");

            // test DB Connection immediately on startup
            DBConnection.getInstance();

            // create stored procedure + trigger if not already present
            DBSchemaInitializer.runAll();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("OceanView System Shutting Down...");
        try {
            if (DBConnection.getInstance().getConnection() != null) {
                DBConnection.getInstance().getConnection().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
