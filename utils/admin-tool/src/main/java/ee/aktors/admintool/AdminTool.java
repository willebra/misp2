/*
 * The MIT License
 * Copyright (c) 2020- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2019 Estonian Information System Authority (RIA)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package ee.aktors.admintool;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * @author siim.kinks
 *
 */

public class AdminTool {

    public static final String CONFNAME = "config.cfg";

    public static void main(String[] args) {
        try {
            Console c = System.console();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(
            System.in));
            Connection dbConnection;
            /**
             * 0 - list;
             * positive int - add;
             * -1 - delete;
             * -2 - delete with list
            */
            int action = 0;
            String username = "";
            String[] dbLookables = { "driver", "url", "username", "password" };
            String[] dbData = new String[dbLookables.length];
            String[] commands = {"-delete", "-add", "-config"};
            String confPath = null;
            boolean isPath = false;
            Actions actions = new Actions();
            if (c == null) {
                System.err.println("No console.");
                return;
            }
            //get input
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-") && !contains(commands, args[i])) {
                    System.out.println("Command " + args[i] + " undefined");
                    printHelp(isPath);
                    return;
                }
                if (args[i].equalsIgnoreCase(commands[0])) {
                    action = -2;
                    if ((i + 1) < args.length && !args[i + 1].startsWith("-")) {
                        action = -1;
                        i++;
                        username = args[i];
                    }
                } else if (args[i].equalsIgnoreCase(commands[1])) {
                    action = 1;
                } else if (args[i].equalsIgnoreCase(commands[2])) {
                    if ((i + 1) < args.length  && !args[i + 1].startsWith("-")) {
                        i++;
                        confPath = args[i];
                        isPath = true;
                    }
                }
            }
            System.out.println("\n*** You are using administrator account tool ***");
            String actionMsg = "";
            if (action < 0) {
                actionMsg = "Deleting";
            } else if (action > 0) {
                actionMsg = "Adding";
            } else {
                actionMsg = "Viewing";
            }
            System.out.printf("*** %s action selected ***\n", actionMsg);
            if (action == 0) {
                printHelp(isPath);
            }
            dbData = actions.getData(confPath, isPath, dbLookables);
            if (dbData == null) {
                return;
            }
            //connect to database
            if ((dbConnection = actions.connect(dbData)) == null) {
                return;
            }
            //do actions
            if (action < 0) {
                if (action == -1) {
                    actions.deleteAdmin(dbData, username, dbConnection);
                } else {
                    while (actions.listAdminsWithDelete(dbData, dbConnection)) {
                        System.out.print("Delete more accounts?(Y/N) > ");
                        if (!bfr.readLine().equalsIgnoreCase("y")) {
                                break;
                        }
                    }
                }
            } else if (action > 0) {
                actions.createAdmin(dbData, c, dbConnection);
            } else {
                actions.listAdmins(dbData, dbConnection);
            }
            try {
                dbConnection.close();
            } catch (SQLException e) {
                System.err.println("Couldn't close connection!");
            }
        } catch (Exception e) {
            System.err.println("Error occurred!");
        }

        System.out.println("Program ended! Have a nice day.");
        return;
    }

    private static void printHelp(boolean isPath) {
        String path = "";
        if (!isPath) {
            path = "\tfor specifying " + CONFNAME + " location use parameter: -config\n";
        }
        System.out.println("*** Help ***\n\tfor deleting use parameter: -delete \n" +
            "\tfor adding new use parameter: -add\n" + path);
    }

    private static boolean contains(String[] array, String string) {
        for (String s : array) {
            if (string==null ? s==null : string.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
