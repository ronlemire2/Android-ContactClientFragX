package dev.ronlemire.contactClientFrag;

import org.apache.http.HttpResponse;

import android.app.AlertDialog;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ContactAddFragment extends Fragment {
	public static final String CONTACT_ADDED_INTENT = "dev.ronlemire.contactClientFrag.CONTACT_ADDED";
	public static final String CONTACT_ID = "Id";
	private View rootView;
	private String contactId = "";
	private EditText idEditText;
	private EditText firstNameEditText;
	private EditText lastNameEditText;
	private EditText emailEditText;
	private Button addContactButton;

	// *****************************************************************************
	// Singleton method used to pass variables to a new Fragment instance.
	// *****************************************************************************
	public static ContactAddFragment newInstance(String contactId) {
		ContactAddFragment newContactAddFragment = new ContactAddFragment();
		Bundle argumentsBundle = new Bundle(); // create a new Bundle
		argumentsBundle.putString(CONTACT_ID, contactId);
		newContactAddFragment.setArguments(argumentsBundle);
		return newContactAddFragment;
	}

	// *****************************************************************************
	// Fragment Life Cycle events
	// *****************************************************************************
	// create the Fragment from the saved state Bundle
	@Override
	public void onCreate(Bundle argumentsBundle) {
		super.onCreate(argumentsBundle);
		this.contactId = getArguments().getString(CONTACT_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.contact_add, null);

		idEditText = (EditText) rootView.findViewById(R.id.idEditText);
		idEditText.setText(this.contactId);
		idEditText.setEnabled(false);
		firstNameEditText = (EditText) rootView
				.findViewById(R.id.firstNameEditText);
		lastNameEditText = (EditText) rootView
				.findViewById(R.id.lastNameEditText);
		emailEditText = (EditText) rootView.findViewById(R.id.emailEditText);

		addContactButton = (Button) rootView.findViewById(R.id.addContactButton);
		addContactButton.setOnClickListener(addContactButtonClicked);

		return rootView;
	}
	
	// *****************************************************************************
	// 'Add Contact' button click listener.
	// *****************************************************************************
	OnClickListener addContactButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String id = idEditText.getText().toString(); 
			String firstName = firstNameEditText.getText().toString();
			String lastName = lastNameEditText.getText().toString();
			String email = emailEditText.getText().toString();
			
			// Make sure that the Id, FirstName, and LastName are filled in.
			// Id is set by the system and is ReadOnly.
			if (id.length() > 0 && firstName.length() > 0 && lastName.length() > 0) {
				Contact addContact = new Contact(id, firstName, lastName, email);
				new AddContactAsyncTask(addContact, getActivity(), new ContactAddedListener()).execute(addContact);
			} 
			else {
				// create a new AlertDialog Builder for missing values
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				String errorMessage = "";
				if (firstName.length() == 0) {
					errorMessage = getString(R.string.errorFirstNameMessage);
				}
				else if (lastName.length() == 0){
					errorMessage = getString(R.string.errorLastNameMessage);
				}
				builder.setMessage(errorMessage);
				builder.setPositiveButton(R.string.errorButton, null);
				builder.show(); 
			} 
		} 
	}; 
	
	// ************************************************************************************
	// Add Contact on a separate thread.
	// ************************************************************************************
	public interface ContactAddedListenerInterface {
		public void onContactAdded(Integer statusCode);
	}
	
	public class AddContactAsyncTask extends AsyncTask<Object, Object, Integer> {
		private ContactAddedListener contactAddedListener;
		private Context taskContext;
		HttpResponse response;

		// public constructor
		public AddContactAsyncTask(Contact addContact, Context context,	ContactAddedListener listener) {
			this.contactAddedListener = listener;
			this.taskContext = context;
		} 

		@Override
		protected Integer doInBackground(Object... arg0) {
			Contact addContact = (Contact) arg0[0];
	    	ContactHttpClient contactHttpClient = new ContactHttpClient(this.taskContext);
	    	return contactHttpClient.SaveContact(addContact);
		}
		
		// executed back on the UI thread after the contact deleted
		protected void onPostExecute(Integer statusCode) {
			contactAddedListener.onContactAdded(statusCode);
		} 
	}	

	// listens for contact added in background task
	private class ContactAddedListener implements ContactAddedListenerInterface {

		public ContactAddedListener() {
		} 

		@Override
		public void onContactAdded(Integer newContactId) {
			if (newContactId > 0){
				Toast message = Toast.makeText(getActivity(), getResources().getString(R.string.contactAdded) + " "  + newContactId.toString(), Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER, message.getXOffset(), message.getYOffset());
				message.show();
			}
			else {
				Toast message = Toast.makeText(getActivity(),  R.string.contactAddedErrorMessage, Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER, message.getXOffset(), message.getYOffset());
				message.show();
			}
			
			// Send message to Main Activity that contact has been added
			Intent intent = new Intent(CONTACT_ADDED_INTENT);
			intent.putExtra(CONTACT_ID, newContactId.toString());
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getActivity().sendBroadcast(intent);			
		} 
	} 	
}
