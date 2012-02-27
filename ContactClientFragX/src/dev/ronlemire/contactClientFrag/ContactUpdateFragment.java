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

public class ContactUpdateFragment extends Fragment  {
	public static final String CONTACT_UPDATED_INTENT = "dev.ronlemire.contactClientFrag.CONTACT_UPDATED";
	public static final String EXISTING_CONTACT = "ExistingContact";
	public static final String CONTACT_UPDATED_ID = "Id";
	private View rootView;
	private EditText idEditText;
	private EditText firstNameEditText;
	private EditText lastNameEditText;
	private EditText emailEditText;
	private Button updateContactButton;
	private Contact existingContact;

	// *****************************************************************************
	// Singleton method used to pass variables to a new Fragment instance.
	// *****************************************************************************
	public static ContactUpdateFragment newInstance(Contact contact) {
		ContactUpdateFragment updateContactUpdateFragment = new ContactUpdateFragment();
		Bundle argumentsBundle = new Bundle(); // create a new Bundle
		argumentsBundle.putSerializable(EXISTING_CONTACT, contact);
		updateContactUpdateFragment.setArguments(argumentsBundle);
		return updateContactUpdateFragment;
	}
	
	// *****************************************************************************
	// Fragment Life Cycle events
	// *****************************************************************************
	// create the Fragment from the saved state Bundle
	@Override
	public void onCreate(Bundle argumentsBundle) {
		super.onCreate(argumentsBundle);
		this.existingContact = (Contact) getArguments().getSerializable(EXISTING_CONTACT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.contact_update, null);

		idEditText = (EditText) rootView.findViewById(R.id.idEditText);
		idEditText.setText(existingContact.getId());
		idEditText.setEnabled(false);

		firstNameEditText = (EditText) rootView
				.findViewById(R.id.firstNameEditText);
		firstNameEditText.setText(existingContact.getFirstName());
		
		lastNameEditText = (EditText) rootView
				.findViewById(R.id.lastNameEditText);
		lastNameEditText.setText(existingContact.getLastName());
		
		emailEditText = (EditText) rootView.findViewById(R.id.emailEditText);
		emailEditText.setText(existingContact.getEmail());

		updateContactButton = (Button) rootView.findViewById(R.id.updateContactButton);
		updateContactButton.setOnClickListener(updateContactButtonClicked);

		return rootView;
	}	
	
	// *****************************************************************************
	// Update Contact button click listener.
	// *****************************************************************************
	OnClickListener updateContactButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String id = idEditText.getText().toString(); 
			String firstName = firstNameEditText.getText().toString();
			String lastName = lastNameEditText.getText().toString();
			String email = emailEditText.getText().toString();
			
			// Make sure that the Id, FirstName, and LastName are filled in.
			// Id is set by the system and is ReadOnly.
			if (id.length() > 0 && firstName.length() > 0 && lastName.length() > 0) {
				Contact updateContact = new Contact(id, firstName, lastName, email);
				new UpdateContactAsyncTask(updateContact, getActivity(), new ContactUpdatedListener()).execute(updateContact);
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
	// Update Contact on a separate thread.
	// ************************************************************************************
	public interface ContactUpdatedListenerInterface {
		public void onContactUpdated(Integer statusCode);
	}
	
	public class UpdateContactAsyncTask extends AsyncTask<Object, Object, Integer> {
		private ContactUpdatedListener contactUpdatedListener;
		private Context taskContext;
		HttpResponse response;

		// public constructor
		public UpdateContactAsyncTask(Contact updateContact, Context context,	ContactUpdatedListener listener) {
			this.contactUpdatedListener = listener;
			this.taskContext = context;
		} 

		@Override
		protected Integer doInBackground(Object... arg0) {
			Contact updateContact = (Contact) arg0[0];
	    	ContactHttpClient contactHttpClient = new ContactHttpClient(this.taskContext);
	    	return contactHttpClient.SaveContact(updateContact);
		}
		
		// executed back on the UI thread after the contact deleted
		protected void onPostExecute(Integer statusCode) {
			contactUpdatedListener.onContactUpdated(statusCode);
		} 
	}	

	// listens for contact added in background task
	private class ContactUpdatedListener implements ContactUpdatedListenerInterface {

		public ContactUpdatedListener() {
		} 

		@Override
		public void onContactUpdated(Integer statusCode) {
			if (statusCode == 200){
				Toast message = Toast.makeText(getActivity(), R.string.contactUpdated, Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER, message.getXOffset(), message.getYOffset());
				message.show();
			}
			else {
				Toast message = Toast.makeText(getActivity(),  R.string.contactAddedErrorMessage, Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER, message.getXOffset(), message.getYOffset());
				message.show();
			}
			
			// Send message to Main Activity that contact has been added
			Intent intent = new Intent(CONTACT_UPDATED_INTENT);
			intent.putExtra(CONTACT_UPDATED_ID, existingContact.getId());
			getActivity().sendBroadcast(intent);			
		} 
	} 		
}
