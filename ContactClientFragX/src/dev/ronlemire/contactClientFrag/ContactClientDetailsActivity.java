package dev.ronlemire.contactClientFrag;

import android.app.Activity;

//import android.app.Fragment;
//import android.app.FragmentManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ContactClientDetailsActivity extends ActionBarFragmentActivity {
	private FragmentManager myFragmentManager;
	private ContactLoadedReceiver contactLoadedReceiver;
	private ContactAddedReceiver contactAddedReceiver;
	private ContactUpdatedReceiver contactUpdatedReceiver;
	private ContactDeletedReceiver contactDeletedReceiver;
	private IntentFilter contactAddedFilter;
	private IntentFilter contactUpdatedFilter;
	private IntentFilter contactLoadedFilter;
	private IntentFilter contactDeletedFilter;
	private Contact contactLoaded = null;
	private Context detailsContext;

	// *****************************************************************************
	// Activity Life Cycle events
	// *****************************************************************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(ContactClientMainActivity.TAG,
				"in ContactClientDetailsActivity onCreate");
		super.onCreate(savedInstanceState);
		myFragmentManager = getSupportFragmentManager();

		contactLoadedReceiver = new ContactLoadedReceiver();
		contactLoadedFilter = new IntentFilter(
				ContactViewFragment.CONTACT_LOADED_INTENT);

		contactAddedReceiver = new ContactAddedReceiver();
		contactAddedFilter = new IntentFilter(
				ContactAddFragment.CONTACT_ADDED_INTENT);

		contactUpdatedReceiver = new ContactUpdatedReceiver();
		contactUpdatedFilter = new IntentFilter(
				ContactUpdateFragment.CONTACT_UPDATED_INTENT);

		contactDeletedReceiver = new ContactDeletedReceiver();
		contactDeletedFilter = new IntentFilter(
				ContactDeleteFragment.CONTACT_DELETED_INTENT);

		myFragmentManager = getSupportFragmentManager();
		detailsContext = this;

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& isTablet()) {
			// If the screen is now in landscape mode, it means
			// that our MainActivity is being shown with both
			// the titles and the text, so this activity is
			// no longer needed. Bail out and let the MainActivity
			// do all the work.
			finish();
			return;
		}

		if (getIntent() != null) {
			Bundle b = getIntent().getExtras();
			String fragmentName = b.getString("fragmentName");
			String contactSelectedId = b.getString("contactSelectedId");
			;

			if (fragmentName.equals("ContactAddFragment")) {
				ContactAddFragment contactAddFragment = ContactAddFragment
						.newInstance("0");
				getSupportFragmentManager().beginTransaction()
						.add(android.R.id.content, contactAddFragment).commit();
			} else if (fragmentName.equals("ContactViewFragment")) {
				ContactViewFragment contactViewFragment = ContactViewFragment
						.newInstance(contactSelectedId);
				getSupportFragmentManager().beginTransaction()
						.add(android.R.id.content, contactViewFragment)
						.commit();
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(contactLoadedReceiver, contactLoadedFilter);
		registerReceiver(contactAddedReceiver, contactAddedFilter);
		registerReceiver(contactUpdatedReceiver, contactUpdatedFilter);
		registerReceiver(contactDeletedReceiver, contactDeletedFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(contactLoadedReceiver);
		unregisterReceiver(contactAddedReceiver);
		unregisterReceiver(contactUpdatedReceiver);
		unregisterReceiver(contactDeletedReceiver);
	}

	// *****************************************************************************
	// ActionBar
	// *****************************************************************************
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater(); // global MenuInflator

		// inflate layout defined in actionmenu.xml
		inflater.inflate(R.menu.actionmenu, menu);
		return true; // return true since the menu was created
	}

	// when one of the items was clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			ReturnToContactList();
			return true;
		case R.id.addContactItem:
			StartContactAddFragment();
			return true;
		case R.id.editContactItem:
			StartContactUpdateFragment();
			return true;
		case R.id.deleteContactItem:
			StartContactDeleteFragment();
			return true;
		case R.id.prefsItem:
			ContactClientMainActivity.showPrefsFragmentDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void StartContactAddFragment() {
		Intent intent = new Intent();
		intent.setClass(this, ContactClientDetailsActivity.class);
		intent.putExtra("fragmentName", "ContactAddFragment");
		startActivity(intent);
	}

	private void StartContactUpdateFragment() {
		if (contactLoaded != null) {
			ContactUpdateFragment contactUpdateFragment = ContactUpdateFragment
					.newInstance(new Contact(contactLoaded.getId(),
							contactLoaded.getFirstName(), contactLoaded
									.getLastName(), contactLoaded.getEmail()));
			getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, contactUpdateFragment)
					.commit();
		}
	}

	private void StartContactDeleteFragment() {
		if (contactLoaded != null) {
			Fragment contactDeleteFragment = (Fragment) myFragmentManager
					.findFragmentById(R.id.contact_replacer);
			contactDeleteFragment = ContactDeleteFragment
					.newInstance(new Contact(contactLoaded.getId(),
							contactLoaded.getFirstName(), contactLoaded
									.getLastName(), contactLoaded.getEmail()));
			getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, contactDeleteFragment)
					.commit();
		}
	}

	private void ReturnToContactList() {
		Intent contactListIntent = new Intent();
		contactListIntent.setClass(detailsContext,
				ContactClientMainActivity.class);
		contactListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(contactListIntent);
		contactLoaded = null;
	}

	// *****************************************************************************
	// Helper methods
	// *****************************************************************************
	private boolean isTablet() {
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);

		int width = displayMetrics.widthPixels / displayMetrics.densityDpi;
		int height = displayMetrics.heightPixels / displayMetrics.densityDpi;

		double screenDiagonal = Math.sqrt(width * width + height * height);
		return (screenDiagonal >= 8.5);
	}
	
	// *****************************************************************************
	// BroadcastReceivers
	// *****************************************************************************
	class ContactLoadedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			contactLoaded = (Contact) intent
					.getSerializableExtra(ContactViewFragment.CONTACT_LOADED);
			Log.d("ContactLoadedReceiver",
					"Selected Id: " + contactLoaded.getId());
		}
	}

	class ContactAddedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newContactId = intent
					.getStringExtra(ContactAddFragment.CONTACT_ID);
			Log.d("ContactAddedReceiver", "New Contact Id: " + newContactId);
			ReturnToContactList();
		}
	}

	class ContactUpdatedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String existingContactId = intent
					.getStringExtra(ContactUpdateFragment.CONTACT_UPDATED_ID);
			Log.d("ContactUpdatedReceiver", "Existing Contact Id: "
					+ existingContactId);
			ReturnToContactList();
		}
	}

	class ContactDeletedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String deleteContactId = intent
					.getStringExtra(ContactDeleteFragment.CONTACT_DELETED_ID);
			Log.d("ContactDeletedReceiver", "Delete Contact Id: "
					+ deleteContactId);
			ReturnToContactList();
		}
	}
}
