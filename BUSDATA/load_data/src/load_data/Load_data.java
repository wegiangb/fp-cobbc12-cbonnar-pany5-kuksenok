package load_data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Load_data {

    public static void main(String[] args) throws SQLException, IOException {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/bus2?user=root&password=");
        Stop.init(conn);

        String[] gtfs_dates = new String[]{"2011_11", "2011_12", "2012_03_01", "2012_03_24", "2012_04_11"};
        for (String gtfs_date : gtfs_dates) {
            System.out.println("Added stops for " + gtfs_date + " : " + Stop.pushFromGtfsFile("../DATA/" + gtfs_date + " stops.txt", gtfs_date));

        }

        String[] oba_dates = new String[]{"2011-12-16", "2011-12-17", "2011-12-18", "2011-12-21", "2012-03-02", "2012-03-03", "2012-03-04", "2012-03-07"};
        for (String oba_date : oba_dates) {
            // 
        }
    }

    /**
     * Count the amount of tuples in a file-  ignoring header row!
     * @param path
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static int count(String path) throws FileNotFoundException, IOException {
        FileReader fr = new FileReader(path);
        BufferedReader tr = new BufferedReader(fr);
        String line = tr.readLine();
        int result = 0;
        while ((line = tr.readLine()) != null) {
            result++;
        }
        fr.close();
        return result;
    }

}

class Stop {

    int stop_id;
    double lat;
    double lon;
    String gtfs_data;
    static PreparedStatement write = null;
    static PreparedStatement count = null;

    public static void init(Connection conn) throws SQLException {
        write = conn.prepareStatement("insert into stops set stop_id=?, lat=?, lon=?, gtfs_data=?");
        count = conn.prepareStatement("select count(*) from stops where gtfs_data=?");
    }

    public static int count(String gtfs_data) throws SQLException {
        count.setString(1, gtfs_data);
        ResultSet rs = count.executeQuery();
        if (rs.first()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public int write() throws SQLException {
        write.setInt(1, stop_id);
        write.setDouble(2, lat);
        write.setDouble(3, lon);
        write.setString(4, gtfs_data);
        try {
            return write.executeUpdate();
        } catch (Exception e) {
            return 0;
        }
    }

    public static int pushFromGtfsFile(String path, String gtfs_data) throws FileNotFoundException, IOException, SQLException {
        if(Stop.count(gtfs_data) == Load_data.count(path)) {
            return 0;
        }
        FileReader fr = new FileReader(path);
        BufferedReader tr = new BufferedReader(fr);
        String line = tr.readLine();
        /*
         Header row:
         stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,wheelchair_boarding,stop_direction
         */
        int result = 0;
        while ((line = tr.readLine()) != null) {
            String[] parts = line.split(",");
            Stop s = new Stop();
            s.lat = Double.parseDouble(parts[parts.length - 7]);
            s.lon = Double.parseDouble(parts[parts.length - 6]);
            s.gtfs_data = gtfs_data;
            s.stop_id = Integer.parseInt(parts[0]);
            result += s.write();
        }
        fr.close();
        return result;
    }
}

class Schedule {

    int stop_id;
    double lat;
    double lon;
    String gtfs_data;
}

class Trip {

    int trip_id;
    int route_id;
}

class Route {

    int route_id;
    String route_short_name;
    String route_headsign;
    String gtfs_date;
}

class Deviation {

    int stop_id;
    int trip_id;
    int deviation;
    long time;
}
