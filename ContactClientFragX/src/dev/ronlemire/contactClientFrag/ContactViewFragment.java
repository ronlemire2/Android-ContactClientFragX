package dev.ronlemire.contactClientFrag;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContactViewFragment extends Fragment {
	public static final String CONTACT_LOADED_INTENT = "dev.ronlemire.contactClientFrag.CONTACT_LOADED";
	public static final String CONTACT_LOADED = "ContactLoaded";
	public static final String CONTACT_LOADED_ID = "Id";
	public static final String CONTACT_LOADED_FIRSTNAME = "FirstName";
	public static final String CONTACT_LOADED_LASTNAME = "LastName";
	public static final String CONTACT_LOADED_EMAIL = "Email";
	private static final String CONTACT_ID = "Id";
	private TextView idTextView;
	private TextView firstNameTextView;
	private TextView lastNameTextView;
	private TextView emailTextView;
	private Context context;
	private View rootView;
	private String contactSelectedId = "";

	// *****************************************************************************
	// Singleton method used to pass variables to a new Fragment instance.
	// *****************************************************************************
	public static ContactViewFragment newInstance(String contactSelectedId) {
		ContactViewFragment newContactViewFragment = new ContactViewFragment();
		Bundle argumentsBundle = new Bundle(); // create a new Bundle
		argumentsBundle.putString(CONTACT_ID, contactSelectedId);
		newContactViewFragment.setArguments(argumentsBundle);
		return newContactViewFragment;
	}

	// *****************************************************************************
	// Fragment Life Cycle events
	// *****************************************************************************
	// create the Fragment from the saved state Bundle
	@Override
	public void onCreate(Bundle argumentsBundle) {
		super.onCreate(argumentsBundle);
		this.contactSelectedId = getArguments().getString(CONTACT_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.contact_view, null);
		context = rootView.getContext();

		idTextView = (TextView) rootView.findViewById(R.id.idTextView);
		firstNameTextView = (TextView) rootView
				.findViewById(R.id.firstNameTextView);
		lastNameTextView = (TextView) rootView
				.findViewById(R.id.lastNameTextView);
		emailTextView = (TextView) rootView.findViewById(R.id.emailTextView);

		GetContact(contactSelectedId);

		return rootView;
	}

	public void GetContact(String contactId) {
		new GetContactAsyncTask("", context, new ContactLoadedListener())
				.execute(contactId);
	}

	// **********************************************************************************
	// Get Contact on a separate thread.
	// Note: Contact will be returned by ContactService in a one element
	// ContactArray.
	// **********************************************************************************
	public interface ContactLoadedListenerInterface {
		public void onContactLoaded(Contact[] contactsArray);
	}

	class GetContactAsyncTask extends AsyncTask<Object, Object, String> {
		String idParameterString;
		private Context context; // launching Activity's Context
		private Contact[] contactsArray;

		private ContactLoadedListener contactLoadedListener;

		public GetContactAsyncTask(String idParameterString, Context context,
				ContactLoadedListener contactLoadedListener) {
			this.idParameterString = idParameterString;
			this.context = context;
			this.contactLoadedListener = contactLoadedListener;
		}

		// Call ContactService to get Contact
		@Override
		protected String doInBackground(Object... params) {
			ContactHttpClient contactHttpClient = new ContactHttpClient(
					this.context);
			contactsArray = contactHttpClient
					.GetContactArray((String) params[0]);
			return null;
		}

		// executed back on the UI thread after the contact loads
		protected void onPostExecute(String domainString) {
			contactLoadedListener.onContactLoaded(contactsArray);
		}
	}

	public class ContactLoadedListener implements
			ContactLoadedListenerInterface {
		public ContactLoadedListener() {
		}

		@Override
		public void onContactLoaded(Contact[] contactsArray) {
			if (contactsArray != null) {
				idTextView.setText(contactsArray[0].getId());
				firstNameTextView.setText(contactsArray[0].getFirstName());
				lastNameTextView.setText(contactsArray[0].getLastName());
				emailTextView.setText(contactsArray[0].getEmail());

				// Send message to Main Activity that contact has been loaded
				Intent intent = new Intent(CONTACT_LOADED_INTENT);
				// putExtra(key, serializable)
				intent.putExtra(
						CONTACT_LOADED,
						new Contact(contactsArray[0].getId(), contactsArray[0]
								.getFirstName(),
								contactsArray[0].getLastName(),
								contactsArray[0].getEmail()));
				getActivity().sendBroadcast(intent);
			}
		}
	}

}
