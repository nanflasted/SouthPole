package neutralspacedev.southpole;

import android.os.AsyncTask;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import GenericClient.Client;
import Utility.SouthPoleUtil;

public class ContactServer extends AsyncTask<SouthPoleUtil.Command, Integer, SouthPoleUtil.ServerResponse> {

    protected SouthPoleUtil.ServerResponse doInBackground(SouthPoleUtil.Command... c) {
        return Client.sendServerCommand(c[0]);
    }

}
