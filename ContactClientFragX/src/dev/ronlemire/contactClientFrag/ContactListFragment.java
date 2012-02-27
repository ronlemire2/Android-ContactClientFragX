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
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

public class ContactListFragment extends Fragment {
	public static final String CONTACT_SELECTED_INTENT = "dev.ronlemire.contactClientFrag.CONTACT_SELECTED";
	public static final String CONTACT_SELECTED_ID = "Id";
	public static String LIST_CONTACT_ID; // View contact Intent key
	private Context context;
	private View rootView;
	private TableLayout queryTableLayout; // shows the search buttons
	private ProgressBar pbLoading;
	private TextView tvLoading;

	// *****************************************************************************
	// Singleton method used to pass variables to a new Fragment instance.
	// *****************************************************************************
	public static ContactListFragment newInstance() {
		ContactListFragment newContactListFragment = new ContactListFragment();
		return newContactListFragment;
	}

	// *****************************************************************************
	// Fragment Life Cycle events
	// *****************************************************************************
	// create the Fragment from the saved state Bundle
	@Override
	public void onCreate(Bundle argumentsBundle) {
		super.onCreate(argumentsBundle);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.contact_list, null);
		context = rootView.getContext();

		Button runServiceButton = (Button) rootView
				.findViewById(R.id.runServiceButton);
		runServiceButton.setOnClickListener(refreshButtonListener);

		// get a reference to the queryTableLayout
		queryTableLayout = (TableLayout) rootView
				.findViewById(R.id.contactsTableLayout);

		// btnRun = (Button) this.findViewById(R.id.runServiceButton);
		// btnRun.setVisibility(View.INVISIBLE);
		pbLoading = (ProgressBar) rootView.findViewById(R.id.pbLoading);
		pbLoading.setVisibility(View.INVISIBLE);
		tvLoading = (TextView) rootView.findViewById(R.id.tvLoading);
		tvLoading.setVisibility(View.INVISIBLE);

		TableLayout table = (TableLayout) rootView
				.findViewById(R.id.contactsTableLayout);
		table.removeAllViews();

		pbLoading.setVisibility(View.VISIBLE);
		tvLoading.setVisibility(View.VISIBLE);

		GetContactList();

		return rootView;
	}

	// *****************************************************************************
	// 'Refresh List' Button listener
	// *****************************************************************************
	public OnClickListener refreshButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			GetContactList();
			//StartContactEmptyFragment();
		}
	};

	public void GetContactList() {
		TableLayout table = (TableLayout) rootView
				.findViewById(R.id.contactsTableLayout);
		table.removeAllViews();

		pbLoading.setVisibility(View.VISIBLE);
		tvLoading.setVisibility(View.VISIBLE);

		new GetContactListAsyncTask("", context,
				new ContactListLoadedListener()).execute("0");
	}

	// *****************************************************************************
	// GetContactList on a separate thread
	// *****************************************************************************
	public interface ContactListLoadedListenerInterface {
		public void onContactListLoaded(Contact[] contactsArray);
	}

	public class GetContactListAsyncTask extends
			AsyncTask<Object, Object, String> {
		String idParameterString;
		private Context context;
		private Contact[] contactsArray;

		// listener for retrieved ContactList
		private ContactListLoadedListener contactListLoadedListener;

		public GetContactListAsyncTask(
				String idParameterString,
				Context context,
				dev.ronlemire.contactClientFrag.ContactListFragment.ContactListLoadedListener contactListLoadedListener) {
			this.idParameterString = idParameterString;
			this.context = context;
			this.contactListLoadedListener = (ContactListLoadedListener) contactListLoadedListener;
		}

		// Call ContactService to get ContactList
		@Override
		protected String doInBackground(Object... params) {
			ContactHttpClient contactHttpClient = new ContactHttpClient(
					this.context);
			contactsArray = contactHttpClient
					.GetContactArray((String) params[0]);

			return null; // return null if the city name couldn't be found
		}

		// executed back on the UI thread after the city name loads
		protected void onPostExecute(String domainString) {
			contactListLoadedListener.onContactListLoaded(contactsArray);
		} // end method onPostExecute

	}

	// listens for contacts loaded in background task
	public class ContactListLoadedListener implements
			ContactListLoadedListenerInterface {

		// create a new CityNameLocationLoadedListener
		public ContactListLoadedListener() {
		} // end CityNameLocationLoadedListener

		@Override
		public void onContactListLoaded(Contact[] contactsArray) {
			for (int i = 0; i < contactsArray.length; i++) {
				makeNewContactRow(((Contact) contactsArray[i]).getId(),
						contactsArray[i].getFirstName(),
						contactsArray[i].getLastName(), i);
			}

			pbLoading.setVisibility(View.INVISIBLE);
			tvLoading.setVisibility(View.INVISIBLE);
		}
	}

	// ***************************************************************************************
	// Inflate new ContactList row.
	// Id column value is underlined and has a listener to start
	// ContactViewActivity.
	// ***************************************************************************************
	private void makeNewContactRow(String ID, String FirstName,
			String LastName, int index) {
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View newContactRowView = inflater.inflate(R.layout.contact_list_row,
				null);

		TextView newIDTextView = (TextView) newContactRowView
				.findViewById(R.id.tvContactID);
		SpannableString content = new SpannableString(ID);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		newIDTextView.setText(content);
		newIDTextView.setOnClickListener(idTextViewListener);

		TextView newFirstNameTextView = (TextView) newContactRowView
				.findViewById(R.id.tvContactFirstName);
		newFirstNameTextView.setText(FirstName);

		TextView newLastNameTextView = (TextView) newContactRowView
				.findViewById(R.id.tvContactLastName);
		newLastNameTextView.setText(LastName);

		// Add new row to ContactList
		queryTableLayout.addView(newContactRowView, index);
	}

	// *****************************************************************************
	// ContactId Column click listener
	// *****************************************************************************
	public OnClickListener idTextViewListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			LIST_CONTACT_ID = ((TextView) v).getText().toString();
			Intent intent = new Intent(CONTACT_SELECTED_INTENT);
			intent.putExtra(CONTACT_SELECTED_ID, LIST_CONTACT_ID);
			getActivity().sendBroadcast(intent);
		}
	};

}
