package com.project.android.bicing;

/**
 * Created by Martin on 09/06/2015.
 */
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class BicingDataXmlParser {

    private static final String ns = null;


    public List<Station> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
           // parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readStations(parser);
        } finally {
            in.close();
        }
    }

    private List<Station> readStations(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Station> st = new ArrayList<Station>();

       // parser.require(XmlPullParser.START_TAG, ns, "station");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("station")) {
                st.add(readStation(parser));
            } else {
                skip(parser);
            }
        }
        return st;
    }

    public static class Station {
        public final String lat;
        public final String lon;
        public final String street;
        public final String slots;
        public final String bikes;
        public final String id;
        public final String streetnumber;

        private Station(String lat, String lon, String street, String slots, String bikes, String id, String streetnumber) {
            this.lat = lat;
            this.lon = lon;
            this.street = street;
            this.slots = slots;
            this.bikes = bikes;
            this.id = id;
            this.streetnumber = streetnumber;
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Station readStation(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "station");
        String lat = null;
        String lon = null;
        String street = null;
        String slots = null;
        String bikes = null;
        String id = null;
        String streetNumber = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("lat")) {
                lat = readText(parser);
            } else if (name.equals("long")) {
                lon = readText(parser);
            } else if (name.equals("street")) {
                street = readText(parser);
            } else if (name.equals("slots")) {
                slots = readText(parser);
            } else if (name.equals("bikes")) {
                bikes = readText(parser);
            } else if (name.equals("id")) {
                id = readText(parser);
            } else if (name.equals("streetNumber")) {
                streetNumber = readText(parser);
            } else {
                skip(parser);
            }
        }
        return new Station(lat, lon, street, slots, bikes, id, streetNumber);
    }


    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
           // Log.d("Data", datos);
            parser.nextTag();
        }
        return result;
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
