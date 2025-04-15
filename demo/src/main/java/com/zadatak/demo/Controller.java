package com.zadatak.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class Controller {
    @GetMapping(path = "/addProduct")
    public String addProduct(@RequestParam String name, @RequestParam String code, @RequestParam String description, @RequestParam float price){
        //Konverzija rijeci u decimalni broj, te onda u prirodni broj
        StringBuilder result = new StringBuilder();

        try {
            URL url = URI.create("https://api.hnb.hr/tecajn-eur/v3?valuta=USD").toURL(); 
            HttpURLConnection conversionConnection = (HttpURLConnection) url.openConnection();
            conversionConnection.setRequestMethod("GET");
            //InputStream is = conn.getInputStream();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conversionConnection.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; )
                    result.append(line);
            }
            conversionConnection.disconnect();
        }
        catch (ProtocolException ex){ System.out.println("Network error!"); }
        catch (MalformedURLException ex){ System.out.println("Site unreachable!"); }
        catch (Exception ex){ ex.printStackTrace(); }

        String str = result.toString();
        double countUSD = Double.parseDouble(str.substring(str.indexOf("srednji_tecaj") + 16, str.indexOf("valuta") - 3).replace(",", "."));

        String returnVal = "";
        // Zapisati podatak u bazu podataka
        try {
            Class.forName("org.h2.Driver");
            Connection connectionDatabase = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
            Statement st = connectionDatabase.createStatement();
            countUSD *= price;
            String upit = "Insert into product (code, name, price_eur, price_usd, description) values('" + code + "','" + name + "'," + price + "," +  countUSD + ",'" + description + "');";
            //System.out.println("Query: " + upit);
            st.execute(upit);
            connectionDatabase.close();
            returnVal = "The product has been recorded";
        }
        catch (SQLException ex) {
            returnVal = "Connection to database has failed!";
        }
        catch (ClassNotFoundException ex) {
            returnVal = "Class has not been found!";
        }
        finally { return returnVal; }
    }
    
    @GetMapping(path = "/addReview")
    public String addReview(@RequestParam String name, @RequestParam int productId, @RequestParam String review, @RequestParam int rating){
        //System.out.println(n * Double.parseDouble(getHTML("https://api.hnb.hr/tecajn-eur/v3?valuta=USD")));
        String returnVal = "";
        try{
            Class.forName("org.h2.Driver");
            Connection connectionDatabase = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
            Statement st = connectionDatabase.createStatement();

            String upit = "insert into review (product_id, reviewer, text, rating) values (" + productId + ",'" + name + "','" + review + "'," + rating + ")";
            System.out.println("Upit: " + upit);
            st.execute(upit);
            connectionDatabase.close();
            returnVal = "Review has been recorded";
        }
        catch (SQLException ex) {
            returnVal = "Executing query has failed!";
        }
        catch (ClassNotFoundException ex) {
            returnVal = "Connecting to database has failed!";
        }
        finally { return returnVal; }
    }

    @GetMapping(path = "/getProducts")
    public String getProducts(){
        String returnVal = "";
        try{
            Class.forName("org.h2.Driver");
            Connection connectionDatabase = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
            Statement st = connectionDatabase.createStatement();
            String upit = "select top 3 product.name as name, avg(review.rating::text::integer) as average from product join review on review.product_id = product.id group by product_id order by average desc";
            ResultSet redak = st.executeQuery(upit);

            JSONObject jsObject;
            JSONArray jsArray = new JSONArray();            

            while(redak.next()){
                jsObject = new JSONObject();
                jsObject.put("averageRating", (redak.getString("average")));//"%.1f".format(redak.getString("ave")));
                jsObject.put("name", redak.getString("name"));
                jsArray.put(jsObject);
            }
            jsObject = new JSONObject();
            jsObject.put("popularProducts", jsArray);
            connectionDatabase.close();
            returnVal = jsObject.toString();
        }
        catch (SQLException ex) {
            returnVal = "Executing query has failed!";
        }
        catch (ClassNotFoundException ex) {
            returnVal = "Connecting to database has failed!";
        }
        finally { return returnVal; }
    }

	public static void main(String[] args){
		SpringApplication.run(Controller.class, args);
		System.out.println("Server started, listening at https://localhost:8080/");
    }
}
