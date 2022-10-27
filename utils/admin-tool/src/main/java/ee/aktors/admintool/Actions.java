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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author siim.kinks
 *
 */
public class Actions {
    private static final String DGST_ALG = "SHA-1";
    private static final int ITERATIONS = 100;

    private String driver = "";

    /**
     * 
     * @param dbData
     * @return connection to database or null if error occurred.
    */
    public Connection connect(String[] dbData) {
        Connection dbConnection = null;
        try {
            Class.forName(dbData[0]); //Or any other driver
            setDriver(dbData[0]);
            System.out.println("Connecting to database...");
            dbConnection = DriverManager.getConnection(dbData[1], dbData[2],
            dbData[3]);
            System.out.println("Connected!\n");
        } catch(SQLException x) {
            System.err.println("Couldn't get connection!");
        } catch(ClassNotFoundException x) {
            System.err.println( "Unable to load the driver class!" );
        }
        return dbConnection;
    }

    private void printTable(String columnName1, String columnName2, int maxLen1, int maxLen2, boolean isHeader, int rowNr, String maxRows) {
        String sep = "|";
        if (isHeader) {
            System.out.printf(" %" + maxRows.length() + "s | ", "#");
            int halfCenter = maxLen1 - columnName1.length();
            if (halfCenter < 0) {
                halfCenter = 1;
            }
            System.out.printf("%s %" + halfCenter + "s ", columnName1, sep);
            System.out.printf("%" + (maxLen2 / 2) + "s\n", columnName2);
            for (int i = 0; i < (maxLen1 + maxLen2 + maxRows.length() + 6); i++) {
                System.out.printf("-");
            }
            System.out.printf("\n");
        } else {
            System.out.printf(" %" + maxRows.length() + "s | ", rowNr);
            System.out.printf("%s %" + (maxLen1 - columnName1.length()) + "s ", columnName1, sep);
            System.out.printf("%s\n", columnName2);
        }
    }

    public boolean listAdmins(String[] dbData, Connection dbConnection) {
        return listAdmins(dbData, dbConnection, false);
    }

    public boolean listAdminsWithDelete(String[] dbData, Connection dbConnection) {
        return listAdmins(dbData, dbConnection, true);
    }

    public boolean listAdmins(String[] dbData, Connection dbConnection, boolean isDelete) {
        BufferedReader bfr = new BufferedReader(new InputStreamReader(System.in));
        ArrayList<String[]> result = new ArrayList<String[]>();
        boolean ret = false;
        try {
            ResultSet resultSet = dbConnection.createStatement().executeQuery("SELECT login_username, created FROM admin ORDER BY login_username");
            int maxName = 8;
            int maxDate = 7;
            String username = null;
            String created = null;
            while(resultSet.next()) {
                username = resultSet.getString("login_username");
                created = resultSet.getString("created");
                result.add(new String[]{username, created});
                if (username.length() > maxName) {
                    maxName = username.length();
                }
                if (created.length() > maxDate) {
                    maxDate = created.length();
                }
            }
            maxName++;
            maxDate++;
            System.out.println("*** Administrators ***\n");
            printTable("Username", "Created", maxName, maxDate, true, 0, String.valueOf(result.size()));
            int i = 1;
            for (String[] s : result) {
                printTable(s[0], s[1], maxName, maxDate, false, i, String.valueOf(result.size()));
                i++;
            }
            if (isDelete) {
                if (result.size() > 0) {
                    while(true) {
                        System.out.print("Enter account number for deleting(\"q\" for exit) > ");
                        String tmp = bfr.readLine();
                        if (tmp.equalsIgnoreCase("q")) {
                            ret = false;
                            break;
                        } else {
                            if (tmp.matches("\\d+")) {
                                int nr = Integer.parseInt(tmp);
                                if (nr <= result.size() && nr != 0) {
                                    deleteAdmin(dbData, result.get(nr - 1)[0], dbConnection);
                                    ret = true;
                                    break;
                                } else {
                                    System.err.println("Wrong number!");
                                }
                            } else {
                                System.err.println("Please enter positive numeric value!");
                            }
                        }
                    }
                } else {
                    System.out.println("There are no accounts!");
                }
            }
        } catch (SQLException se) {
            System.err.println("SQL error!");
        } catch (IOException ioe) {
            System.err.println("IO Error!");
        }
        return ret;
    }

    public boolean deleteAdmin(String[] dbData, String username, Connection dbConnection) {
        boolean ret = false;
        BufferedReader bfr = new BufferedReader(new InputStreamReader(System.in));
        try {
            if (dbConnection.createStatement().executeQuery(
                "SELECT id FROM admin WHERE login_username = '" + username + "'").next()) {
                    System.out.print("Are you shure you want to delete administrator \"" + username + "\"?(Y/N) > ");
                if (bfr.readLine().equalsIgnoreCase("y")) {
                    dbConnection.createStatement().executeUpdate(
                        "DELETE FROM admin WHERE login_username = '" + username + "'");
                    System.out.println("Delete successful!");
                    ret = true;
                } else {
                    System.out.println("Deletion was canceled.");
                    ret = false;
                }
            } else {
                System.err.println("Entered username doesn't exist!");
                ret = false;
            }
        } catch (SQLException x) {
            System.err.println("SQL error!");
        } catch (IOException ioe) {
            System.err.println("IO Error!");
        }
        return ret;
    }

    public boolean createAdmin(String[] dbData, Console c, Connection dbConnection) {
        String username;
        String password = "";
        boolean b;
        char [] tmpPassword = null;
        try {
            while (true) {
                username = c.readLine("Enter new administrator username > ");
                if (username.length() > 50) {
                    c.printf("Entered username is too long!\n");
                    continue;
                }
                try {
                    if (dbConnection.createStatement().executeQuery(
                        "SELECT id FROM admin WHERE login_username = '" + username + "'").next()) {
                        c.printf("Entered username already exists!\n");
                    } else {
                        break;
                    }
                } catch (SQLException x) {
                    System.err.println("SQL error!");
                    break;
                }
            }
            c.printf("Selected username: \"%s\"\n", username);
            b = true;
            while(b) {
                tmpPassword = c.readPassword("Enter password > ");
                if (tmpPassword.length < 50) {
                    if (Arrays.equals(tmpPassword, c.readPassword("Enter password again > "))) {
                        b = false;
                    } else {
                    System.err.println("Passwords don't match!");
                    }
                } else {
                    c.printf("Enetered password is too long!\n");
                }
            }
            password = new String(tmpPassword);
            byte[] salt = getSalt();
            String hash = getBase64String(getHash(username + password, salt, ITERATIONS));

            try {
                String id = "";
                String idValue = "";
                if (getDriver().toLowerCase().contains("oracle")) {
                    id = "ID, ";
                    idValue = "admin_id_seq.nextval, ";
                }
                String insert = "INSERT INTO ADMIN (" + id + "USERNAME, PASSWORD, LOGIN_USERNAME, SALT) VALUES ("
                + idValue + "'program', '" + hash + "', '" + username + "', '" + getBase64String(salt) + "')";

                dbConnection.createStatement().executeUpdate(insert);
                b = true;
            } catch (SQLException x) {
                System.err.println("Account creating failed!");
                b = false;
            }
            if (b) {
                c.printf("Administrator created with username: \"%s\"\n", username);
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Failed creating admin password.");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Failed creating admin password.");
        }
        return false;
    }

    public String[] getData(String confPath, boolean isPath, String[] dbLookables) throws IOException {
        BufferedReader bfr = new BufferedReader(new InputStreamReader(System.in));
        FileReader reader;
        int dataCount = 0;
        int index = 0;
        int tmpIndex = 0;
        String tmp;
        String[] dbData = new String[dbLookables.length];
        String[] stringArray = null;
        while (true) {
            if (!isPath) {
                System.out.print("Enter path to \"" + AdminTool.CONFNAME +"\" > ");
                confPath = bfr.readLine();
            }
            try {
                if (!confPath.endsWith(AdminTool.CONFNAME)) {
                    System.err.println("Enetered value didn't contain path to \"" + AdminTool.CONFNAME + "\"");
                    isPath = false;
                    continue;
                }
                reader = new FileReader(confPath);
                break;
            } catch (FileNotFoundException fnf) {
                isPath = false;
                System.err.println(AdminTool.CONFNAME + " doesn't exist in that directory!");
            }
        }
        bfr = new BufferedReader(reader);
        try {
            System.out.println("Reading data...");
            while ((tmp = bfr.readLine()) != null) {
                if (!tmp.startsWith("#") && (index = tmp.indexOf("jdbc")) != -1) {
                    for (int i = 0; i < dbLookables.length; i++) {
                        if ((tmpIndex = tmp.indexOf(dbLookables[i], index)) != -1) {
                            stringArray = tmp.substring(tmpIndex).split("\\=", -1);
                            dbData[i] = stringArray[1].trim();
                            if (stringArray.length > 2) {
                                for (int k = 2; k < stringArray.length; k++) {
                                    dbData[i] += "=" + stringArray[k];
                                }
                            }
                            dataCount++;
                            break;
                        }
                    }
                }
                if (dataCount == dbLookables.length) {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error while reading data!");
            return null;
        }
        reader.close();
        bfr.close();
        if (dataCount == 4) {
            System.out.println("Done!");
            System.out.println("Database URL:\t\t" + dbData[1]);
            System.out.println("\t username:\t" + dbData[2]);
            return dbData;
        } else {
            System.err.println("Insufficient data resources @" + confPath);
            return null;
        }
    }

    private byte[] getSalt () throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte salt[] = new byte[20];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] getHash (String input, byte[] salt, int iterations) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance(DGST_ALG);
        digest.reset();
        digest.update(salt);
        byte[] bInput = digest.digest(input.getBytes("UTF-8"));
        for (int i = 0; i < 100; i++) {
            digest.reset();
            bInput = digest.digest(bInput);
        }
        return bInput;
    }

    private String getBase64String (byte[] input) throws UnsupportedEncodingException {
        return new String(Base64.encodeBase64(input), "UTF-8");
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
