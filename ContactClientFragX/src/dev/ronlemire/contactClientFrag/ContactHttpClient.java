package dev.ronlemire.contactClientFrag;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ContactHttpClient {
	private static final String TAG = ContactHttpClient.class.getSimpleName();
	 private final String HARDCODED_MULTIPLE_CONTACT_URL = "http://192.168.1.64:8085/service.svc/ContactList";
	 private final String HARDCODED_SINGLE_CONTACT_URL = "http://192.168.1.64:8085/service.svc/Contact/";
	private SharedPreferences prefs;
	private String multipleContactUrl = "";
	private String singleContactUrl = "";

	// *****************************************************************************
	// Constructor - get URLs from Shared Preferences
	// *****************************************************************************
	public ContactHttpClient(Context context) {
		this.prefs = context.getSharedPreferences(
				ContactClientMainActivity.SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		multipleContactUrl = this.prefs.getString(ContactClientMainActivity.MULTIPLE_CONTACT_URL_KEY, null);
		singleContactUrl = this.prefs.getString(ContactClientMainActivity.SINGLE_CONTACT_URL_KEY, null);
//		 multipleContactUrl = HARDCODED_MULTIPLE_CONTACT_URL;
//		 singleContactUrl = HARDCODED_SINGLE_CONTACT_URL;
		Log.i(TAG, "ContactHttpClient constructor");
	}

	// *****************************************************************************
	// If Id is zero(0) get ContactList using HTTP GET method with multipleContactUrl.
	// If Id is non-zero get Contact using HTTP GET method with singleContactUrl.
	// ContactService will return a Contact[] in either case.
	// If getting a specific Contact, the Contact[] will contain only 1 Contact.
	// *****************************************************************************
	public Contact[] GetContactArray(String Id) {
		List<Contact> contactList = new ArrayList<Contact>();
		Contact[] contactArray = null;
		String idString = "";
		String firstNameString = "";
		String lastNameString = "";
		String emailString = "";
		String urlString = "";

		if (Id == "0") {
			urlString = multipleContactUrl;
		} else {
			urlString = singleContactUrl + Id;
		}

		try {
			HttpGet request = new HttpGet(urlString);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();

			// Read response data into buffer
			char[] buffer = new char[(int) responseEntity.getContentLength()];
			InputStream stream = responseEntity.getContent();
			InputStreamReader reader = new InputStreamReader(stream);
			reader.read(buffer);
			stream.close();

			JSONArray contacts = new JSONArray(new String(buffer));
			try {
				for (int i = 0; i < contacts.length(); i++) {
					JSONObject e = contacts.getJSONObject(i);
					idString = e.getString("Id");
					firstNameString = e.getString("FirstName");
					lastNameString = e.getString("LastName");
					emailString = e.getString("Email");
					contactList.add(new Contact(idString, firstNameString,
							lastNameString, emailString));
				}
				int contactListSize = contactList.size();
				contactArray = new Contact[contactListSize];
				for (int i = 0; i < contactListSize; i++) {
					contactArray[i] = (Contact) contactList.get(i);
				}
			} catch (JSONException e) {
				Log.e("log_tag", "Error parsing data " + e.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return contactArray;
	}

	// *****************************************************************************
	// Save Contact using HTTP PUT method with singleContactUrl.
	// If Id is zero(0) a new Contact will be added.
	// If Id is non-zero an existing Contact will be updated.
	// HTTP POST could be used to add a new Contact but the ContactService knows
	// 	an Id of zero means a new Contact so in this case the HTTP PUT is used.
	// *****************************************************************************
	public Integer SaveContact(Contact saveContact) {
		Integer statusCode = 0;
		HttpResponse response;

		try {
			boolean isValid = true;

			// Data validation goes here

			if (isValid) {

				// POST request to <service>/SaveVehicle
				HttpPut request = new HttpPut(singleContactUrl
						+ saveContact.getId());
				request.setHeader("User-Agent", "dev.ronlemire.contactClient");
				request.setHeader("Accept", "application/json");
				request.setHeader("Content-type", "application/json");

				// Build JSON string
				JSONStringer contact = new JSONStringer().object().key("Id")
						.value(Integer.parseInt(saveContact.getId()))
						.key("FirstName").value(saveContact.getFirstName())
						.key("LastName").value(saveContact.getLastName())
						.key("Email").value(saveContact.getEmail()).endObject();
				StringEntity entity = new StringEntity(contact.toString());

				request.setEntity(entity);

				// Send request to WCF service
				DefaultHttpClient httpClient = new DefaultHttpClient();
				response = httpClient.execute(request);

				Log.d("WebInvoke", "Saving : "
						+ response.getStatusLine().getStatusCode());

				// statusCode =
				// Integer.toString(response.getStatusLine().getStatusCode());
				statusCode = response.getStatusLine().getStatusCode();
				
				if (saveContact.getId().equals("0")) {
					// New Contact.Id is in buffer
					HttpEntity responseEntity = response.getEntity();
					char[] buffer = new char[(int) responseEntity.getContentLength()];
					InputStream stream = responseEntity.getContent();
					InputStreamReader reader = new InputStreamReader(stream);
					reader.read(buffer);
					stream.close();
					statusCode = Integer.parseInt(new String(buffer));
				}
				else {
					statusCode = response.getStatusLine().getStatusCode();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return statusCode;
	}

	// *****************************************************************************
	// Delete Contact using HTTP DELETE method with singleContactUrl.
	// *****************************************************************************
	public Integer DeleteContact(Contact deleteContact) {
		Integer statusCode = 0;
		HttpResponse response;

		try {
			boolean isValid = true;

			// Data validation goes here

			if (isValid) {
				HttpDelete request = new HttpDelete(singleContactUrl
						+ deleteContact.getId());
				request.setHeader("User-Agent", "dev.ronlemire.contactClient");
				request.setHeader("Accept", "application/json");
				request.setHeader("Content-type", "application/json");

				// Send request to WCF service
				DefaultHttpClient httpClient = new DefaultHttpClient();
				response = httpClient.execute(request);

				Log.d("WebInvoke", "Saving : "
						+ response.getStatusLine().getStatusCode());

				statusCode = response.getStatusLine().getStatusCode();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return statusCode;
	}
}
