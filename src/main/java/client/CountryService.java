/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import cache.InMemoryCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import model.*;

/**
 *
 * @author Adam
 */
public class CountryService {

    private final String BASEURL = "https://restcountries.com/v3.1/";
    private String JSONSTRING = "";

    private final InMemoryCache CACHE = new InMemoryCache();
    private long cacheTimeInSeconds = 5;

    public ArrayList<Country> getResource(String path) {
        ArrayList<Country> list = queryAPI(path);
        CACHE.add(path, list, cacheTimeInSeconds);
        return list;
    }

    public InMemoryCache getCache() {
        return CACHE;
    }

    public void setCacheExpiry(long seconds) {
        this.cacheTimeInSeconds = seconds;
    }

    public ArrayList<Country> queryAPI(String path) {
        try {
            JSONSTRING = "";
            URL url = new URL(BASEURL + path);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();
            StringBuilder content = new StringBuilder();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            }
            JSONSTRING = content.toString();
            conn.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(CountryService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CountryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parseJSONString(JSONSTRING);
    }

    public ArrayList<Country> parseJSONString(String JSONSTRING) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = null;
        ArrayList<Country> countries = new ArrayList<>();
        try {
            actualObj = mapper.readTree(JSONSTRING);
        } catch (IOException ex) {
            Logger.getLogger(CountryService.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (JsonNode node : actualObj) {
            countries.add(convertJSONToCountryObject(node));
        }
        return countries;
    }

    public Country convertJSONToCountryObject(JsonNode data) {
        Country country = new Country();
        country.setName(generateCountryName(data));
        country.setTld(generateStringArrayList(data, "/tld"));
        country.setCca2(data.path("cca2").textValue());
        country.setCcn3(data.path("ccn3").textValue());
        country.setCca3(data.path("cca3").textValue());
        country.setCioc(data.path("cioc").textValue());
        country.setIndependent(data.path("independent").asBoolean());
        country.setStatus(data.path("status").asText());
        country.setUnMember(data.path("unMember").asBoolean());
        country.setCurrencies(generateCountryCurrency(data));
        country.setIdd(generateIDD(data));
        country.setCapital(generateStringArrayList(data, "/capital"));
        country.setAltSpellings(generateStringArrayList(data, "/altSpellings"));
        country.setRegion(data.path("region").textValue());
        country.setSubregion(data.path("subregion").textValue());
        country.setLanguages(generateLanguages(data));
        country.setTranslations(generateTranslations(data));
        country.setLatlng(generateDoubleArrayList(data, "/latlng"));
        country.setLandlocked(data.path("landlocked").asBoolean());
        country.setBorders(generateStringArrayList(data, "/borders"));
        country.setArea(data.path("area").asDouble());
        country.setDemonyms(generateDemonyms(data));
        country.setFlag(data.path("flag").asText());
        country.setMaps(generateMaps(data));
        country.setPopulation(data.path("population").asInt());
        country.setGini(generateGini(data));
        country.setFifa(data.path("fifa").asText());
        country.setCar(generateCar(data));
        country.setTimezones(generateStringArrayList(data, "/timezones"));
        country.setContinents(generateStringArrayList(data, "/continents"));
        country.setFlags(generatePictureResource(data, "flags"));
        country.setCoatOfArms(generatePictureResource(data, "coatOfArms"));
        country.setStartOfWeek(data.path("startOfWeek").asText());
        country.setCapitalInfo(generateCapitalInfo(data));
        country.setPostalCode(generatePostalCode(data));
        return country;
    }

    public PostalCode generatePostalCode(JsonNode data) {
        PostalCode p = new PostalCode();
        p.setFormat(data.path("postalCode").path("format").asText());
        p.setRegex(data.path("postalCode").path("regex").asText());
        return p;
    }

    public CapitalInfo generateCapitalInfo(JsonNode data) {
        CapitalInfo c = new CapitalInfo();
        c.setLatlng(generateDoubleArrayList(data, "/capitalInfo/latlng"));
        return c;
    }

    public PictureResource generatePictureResource(JsonNode data, String rootNode) {
        PictureResource p = new PictureResource();
        p.setPng(data.path(rootNode).path("png").asText());
        p.setSvg(data.path(rootNode).path("svg").asText());
        return p;
    }

    public Car generateCar(JsonNode data) {
        Car c = new Car();
        c.setSigns(generateStringArrayList(data, "/car/signs"));
        c.setSide(data.path("car").path("side").asText());
        return c;
    }

    public Gini generateGini(JsonNode data) {
        Gini g = new Gini();
        Iterator<String> fieldNames = data.path("gini").fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            g.setValue(data.path("gini").path(fieldName).asDouble());
            g.setYear(fieldName);
        }
        return g;
    }

    public MapResource generateMaps(JsonNode data) {
        MapResource m = new MapResource();
        m.setGoogleMaps(data.path("maps").path("googleMaps").asText());
        m.setOpenStreetMaps(data.path("maps").path("openStreetMaps").asText());
        return m;
    }

    public ArrayList<Demonym> generateDemonyms(JsonNode data) {
        ArrayList<Demonym> list = new ArrayList<>();
        Iterator<String> fieldNames = data.path("demonyms").fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            Demonym d = new Demonym();
            d.setLanguage(fieldName);
            d.setF(data.path("demonyms").path(fieldName).path("f").asText());
            d.setM(data.path("demonyms").path(fieldName).path("m").asText());
            list.add(d);
        }
        return list;
    }

    public ArrayList<CountryNameResource> generateTranslations(JsonNode data) {
        ArrayList<CountryNameResource> list = new ArrayList<>();
        Iterator<String> fieldNames = data.path("translations").fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            CountryNameResource c = new CountryNameResource();
            c.setCca3(fieldName);
            c.setOfficial(data.path("translations").path(fieldName).path("official").asText());
            c.setCommon(data.path("translations").path(fieldName).path("common").asText());
            list.add(c);
        }
        return list;
    }

    public IDD generateIDD(JsonNode data) {
        IDD idd = new IDD();
        idd.setRoot(data.path("idd").path("root").asText());
        idd.setSuffixes(generateStringArrayList(data, "/idd/suffixes"));
        return idd;
    }

    public ArrayList<Language> generateLanguages(JsonNode data) {
        ArrayList<Language> list = new ArrayList<>();
        Iterator<String> fieldNames = data.path("languages").fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            Language l = new Language();
            l.setLang(fieldName);
            l.setLanguage(data.path("languages").path(fieldName).asText());
            list.add(l);
        }
        return list;
    }

    public CountryName generateCountryName(JsonNode data) {
        CountryName countryName = new CountryName();
        countryName.setCommon(data.path("name").path("common").textValue());
        countryName.setOfficial(data.path("name").path("official").textValue());
        ArrayList<CountryNameResource> CNR = new ArrayList<>();
        Iterator<String> fieldNames = data.path("name").path("nativeName").fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            CountryNameResource c = new CountryNameResource();
            c.setCca3(fieldName);
            c.setOfficial(data.path("name").path("nativeName").path(fieldName).path("official").asText());
            c.setCommon(data.path("name").path("nativeName").path(fieldName).path("common").asText());
            CNR.add(c);
        }
        countryName.setNativeName(CNR);
        return countryName;
    }

    public ArrayList<Currency> generateCountryCurrency(JsonNode data) {
        ArrayList<Currency> list = new ArrayList<>();
        Iterator<String> fieldNames = data.path("currencies").fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            Currency c = new Currency();
            c.setShortname(fieldName);
            c.setName(data.path("currencies").path(fieldName).path("name").asText());
            c.setSymbol(data.path("currencies").path(fieldName).path("symbol").asText());
            list.add(c);
        }
        return list;
    }

    public ArrayList<String> generateStringArrayList(JsonNode data, String rootNode) {
        ArrayList<String> list = new ArrayList<>();
        for (JsonNode node : data.at(rootNode)) {
            list.add(node.asText());
        }
        return list;
    }

    public ArrayList<Double> generateDoubleArrayList(JsonNode data, String rootNode) {
        ArrayList<Double> list = new ArrayList<>();
        for (JsonNode node : data.at(rootNode)) {
            list.add(node.asDouble());
        }
        return list;
    }
}
