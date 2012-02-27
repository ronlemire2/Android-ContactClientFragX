package dev.ronlemire.contactClientFrag;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class ContactEmptyFragment extends Fragment {
	private View rootView;
	
	// *****************************************************************************
	// Singleton method used to pass variables to a new Fragment instance.
	// *****************************************************************************
	public static ContactEmptyFragment newInstance() {
		ContactEmptyFragment emptyContactUpdateFragment = new ContactEmptyFragment();
		return emptyContactUpdateFragment;
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
		rootView = inflater.inflate(R.layout.contact_empty, null);
		
		InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(null, 0);
		
		return rootView;
	}
}
