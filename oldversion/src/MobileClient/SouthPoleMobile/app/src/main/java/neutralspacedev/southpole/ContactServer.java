package neutralspacedev.southpole;

import android.os.AsyncTask;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import GenericClient.Client;
import Utility.SPU;

public class ContactServer extends AsyncTask<SPU.Command, Integer, SPU.ServerResponse> {

    protected SPU.ServerResponse doInBackground(SPU.Command... c) {
        return Client.sendServerCommand(c[0]);
    }

}
