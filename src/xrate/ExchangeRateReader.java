package xrate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * Provide access to basic currency exchange rate services.
 */
public class ExchangeRateReader {

    private String accessKey;
    private String baseUrl;

    /**
     * Construct an exchange rate reader using the given base URL. All requests
     * will then be relative to that URL. If, for example, your source is Xavier
     * Finance, the base URL is http://api.finance.xaviermedia.com/api/ Rates
     * for specific days will be constructed from that URL by appending the
     * year, month, and day; the URL for 25 June 2010, for example, would be
     * http://api.finance.xaviermedia.com/api/2010/06/25.xml
     * 
     * @param baseURL
     *            the base URL for requests
     */
    public ExchangeRateReader(String baseURL) throws IOException {
        /*
         * DON'T DO MUCH HERE!
         * People often try to do a lot here, but the action is actually in
         * the two methods below. All you need to do here is store the
         * provided `baseURL` in a field so it will be accessible later.
         */

        this.baseUrl = baseURL;

        // Reads the access keys from `etc/access_keys.properties`
        readAccessKeys();
    }

    /**
     * This reads the `fixer_io` access key from `etc/access_keys.properties`
     * and assigns it to the field `accessKey`.
     *
     * @throws IOException if there is a problem reading the properties file
     */
    private void readAccessKeys() throws IOException {
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            // Don't change this filename unless you know what you're doing.
            // It's crucial that we don't commit the file that contains the
            // (private) access keys. This file is listed in `.gitignore` so
            // it's safe to put keys there as we won't accidentally commit them.
            in = new FileInputStream("etc/access_keys.properties");
        } catch (FileNotFoundException e) {
            /*
             * If this error gets generated, make sure that you have the desired
             * properties file in your project's `etc` directory. You may need
             * to rename the file ending in `.sample` by removing that suffix.
             */
            System.err.println("Couldn't open etc/access_keys.properties; have you renamed the sample file?");
            throw(e);
        }
        properties.load(in);
        // This assumes we're using Fixer.io and that the desired access key is
        // in the properties file in the key labelled `fixer_io`.
        accessKey = properties.getProperty("fixer_io");
    }

    //This method adds a zero to the beginning of a single digit number so the url matches the desired
    //format when a date has single digit numbers, otherwise turns integer into string
    public String dateFixer(int date){
        String newDate;
        if(date<10) {
            newDate = "0" + date;
        } else {
            newDate = Integer.toString(date);
        }
        return newDate;
    }

    //Encapsulate process of getting a URl and getting the rates JSON object from it
    public JsonObject walkThroughJSON(String finalURL) throws IOException {
        try {
            URL url = new URL(finalURL);
            InputStream inputStream = url.openStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            return new JsonParser().parse(reader).getAsJsonObject().getAsJsonObject("rates");

        } catch(IOException e) {
            System.out.println("There was a problem opening the URL. Check if you have the right one");
            throw(e);
        }
    }


    /**
     * Get the exchange rate for the specified currency against the base
     * currency (the Euro) on the specified date.
     * 
     * @param currencyCode
     *            the currency code for the desired currency
     * @param year
     *            the year as a four digit integer
     * @param month
     *            the month as an integer (1=Jan, 12=Dec)
     * @param day
     *            the day of the month as an integer
     * @return the desired exchange rate
     * @throws IOException if there are problems reading from the server
     */
    public float getExchangeRate(String currencyCode, int year, int month, int day) throws IOException{
        String fullURL;
        fullURL = baseUrl + year + "-" + dateFixer(month) + "-" + dateFixer(day) + "?access_key=" + accessKey;
        JsonObject parser = walkThroughJSON(fullURL);
        return parser.get(currencyCode).getAsFloat();
    }

    /**
     * Get the exchange rate of the first specified currency against the second
     * on the specified date.
     * 
     * @param fromCurrency
     *            the currency code we're exchanging *from*
     * @param toCurrency
     *            the currency code we're exchanging *to*
     * @param year
     *            the year as a four digit integer
     * @param month
     *            the month as an integer (1=Jan, 12=Dec)
     * @param day
     *            the day of the month as an integer
     * @return the desired exchange rate
     * @throws IOException if there are problems reading from the server
     */
    public float getExchangeRate(String fromCurrency, String toCurrency, int year,
                                 int month, int day) throws IOException {
        String fullURL;

        fullURL = baseUrl + year + "-" + dateFixer(month) + "-" + dateFixer(day) + "?access_key=" + accessKey;
        JsonObject parser = walkThroughJSON(fullURL);
        float fromCurr = parser.get(fromCurrency).getAsFloat();
        float toCurr = parser.get(toCurrency).getAsFloat();
        return fromCurr/toCurr;
    }
}