package dev.ronlemire.contactClientFrag;

import org.apache.http.HttpResponse;

import android.app.AlertDialog;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
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

public class ContactDeleteFragment extends Fragment {
	public static final String CONTACT_DELETED_INTENT = "dev.ronlemire.contactClientFrag.CONTACT_DELETED";
	public static final String CONTACT_DELETED_ID = "Id";
	public static final String DELETE_CONTACT = "DeleteContact";
	private View rootView;
	private EditText idEditText;
	private EditText firstNameEditText;
	private EditText lastNameEditText;
	private EditText emailEditText;
	private Button deleteContactButton;
	private Contact deleteContact;

	// *****************************************************************************
	// Singleton method used to pass variables to a new Fragment instance.
	// *****************************************************************************
	public static ContactDeleteFragment newInstance(Contact contact) {
		ContactDeleteFragment deleteContactUpdateFragment = new ContactDeleteFragment();
		Bundle argumentsBundle = new Bundle(); 
		argumentsBundle.putSerializable(DELETE_CONTACT, contact);
		deleteContactUpdateFragment.setArguments(argumentsBundle);
		return deleteContactUpdateFragment;
	}

	// *****************************************************************************
	// Fragment Life Cycle events
	// *****************************************************************************
	// create the Fragment from the saved state Bundle
	@Override
	public void onCreate(Bundle argumentsBundle) {
		super.onCreate(argumentsBundle);
		this.deleteContact = (Contact) getArguments().getSerializable(DELETE_CONTACT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.contact_delete, null);

		idEditText = (EditText) rootView.findViewById(R.id.idEditText);
		idEditText.setText(deleteContact.getId());

		firstNameEditText = (EditText) rootView
				.findViewById(R.id.firstNameEditText);
		firstNameEditText.setText(deleteContact.getFirstName());

		lastNameEditText = (EditText) rootView
				.findViewById(R.id.lastNameEditText);
		lastNameEditText.setText(deleteContact.getLastName());

		emailEditText = (EditText) rootView.findViewById(R.id.emailEditText);
		emailEditText.setText(deleteContact.getEmail());

		deleteContactButton = (Button) rootView
				.findViewById(R.id.deleteContactButton);
		deleteContactButton.setOnClickListener(deleteContactButtonClicked);

		return rootView;
	}

	// *****************************************************************************
	// 'Delete Contact' button click listener.
	// *****************************************************************************
	OnClickListener deleteContactButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String id = idEditText.getText().toString();

			// Make sure that the Id, FirstName, and LastName are filled in.
			// Id is set by the system and is ReadOnly.
			if (id.length() > 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Are you sure?")
						.setPositiveButton("Yes", deleteConfirmClickListener)
						.setNegativeButton("No", deleteConfirmClickListener)
						.setTitle("Delete Contact");
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
				alertDialog.getWindow().setLayout(400, 300); //Controlling width and height.
			} else {
				// create a new AlertDialog Builder for missing values
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				String errorMessage = getString(R.string.errorIdMessage);
				builder.setMessage(errorMessage);
				builder.setPositiveButton(R.string.errorButton, null);
				builder.show();
			}
		}
	};

	// *****************************************************************************
	// Delete Contact - Are you sure?  listener
	// *****************************************************************************
	DialogInterface.OnClickListener deleteConfirmClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				Contact deleteContact = new Contact(idEditText.getText().toString(), firstNameEditText.getText().toString(), lastNameEditText.getText().toString(),
						emailEditText.getText().toString());
				new DeleteContactAsyncTask(deleteContact, getActivity(),
						new ContactDeletedListener()).execute(deleteContact);				
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				// No button clicked
				break;
			}
		}
	};

	// ************************************************************************************
	// Update Contact on a separate thread.
	// ************************************************************************************
	public interface ContactDeletedListenerInterface {
		public void onContactDeleted(Integer statusCode);
	}

	public class DeleteContactAsyncTask extends
			AsyncTask<Object, Object, Integer> {
		private ContactDeletedListener contactDeletedListener;
		private Context taskContext;
		HttpResponse response;

		// public constructor
		public DeleteContactAsyncTask(Contact deleteContact, Context context,
				ContactDeletedListener listener) {
			this.contactDeletedListener = listener;
			this.taskContext = context;
		}

		@Override
		protected Integer doInBackground(Object... arg0) {
			Contact deleteContact = (Contact) arg0[0];
			ContactHttpClient contactHttpClient = new ContactHttpClient(
					this.taskContext);
			return contactHttpClient.DeleteContact(deleteContact);
		}

		// executed back on the UI thread after the contact deleted
		protected void onPostExecute(Integer statusCode) {
			contactDeletedListener.onContactDeleted(statusCode);
		}
	}

	// listens for contact added in background task
	private class ContactDeletedListener implements
			ContactDeletedListenerInterface {

		public ContactDeletedListener() {
		}

		@Override
		public void onContactDeleted(Integer statusCode) {
			if (statusCode == 200) {
				Toast message = Toast.makeText(getActivity(),
						R.string.contactDeleted, Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER, message.getXOffset(),
						message.getYOffset());
				message.show();
			} else {
				Toast message = Toast.makeText(getActivity(),
						R.string.contactDeletedErrorMessage, Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER, message.getXOffset(),
						message.getYOffset());
				message.show();
			}

			// Send message to Main Activity that contact has been added
			Intent intent = new Intent(CONTACT_DELETED_INTENT);
			intent.putExtra(CONTACT_DELETED_ID, deleteContact.getId());
			getActivity().sendBroadcast(intent);
		}
	}

}
