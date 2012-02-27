package dev.ronlemire.contactClientFrag;

import android.support.v4.app.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PrefsDialogFragment extends DialogFragment implements
		OnClickListener {
	public interface PrefsFragmentDialogFinishedListener {
		void onDialogFinished(String multipleContactURL, String singleContactURL);
	}

	String multipleContactURL;
	String singleContactURL;
	EditText multipleContactURLEditText;
	EditText singleContactURLEditText;

	public static PrefsDialogFragment newInstance(String multipleContactURL,
			String singleContactURL) {
		PrefsDialogFragment f = new PrefsDialogFragment();

		Bundle args = new Bundle();
		args.putString("multiple", multipleContactURL);
		args.putString("single", singleContactURL);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		multipleContactURL = getArguments().getString("multiple");
		singleContactURL = getArguments().getString("single");

		// allow the user to exit using the back key
		this.setCancelable(true);
	}

	// inflates the DialogFragment's layout
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle argumentsBundle) {
		// inflate the layout defined in add_city_dialog.xml
		View rootView = inflater.inflate(R.layout.prefs_dialog, container,
				false);

		multipleContactURLEditText = (EditText) rootView
				.findViewById(R.id.multiple_contact_url_text);

		singleContactURLEditText = (EditText) rootView
				.findViewById(R.id.single_contact_url_text);
		
		multipleContactURLEditText.setText(multipleContactURL);
		singleContactURLEditText.setText(singleContactURL);

//		if (argumentsBundle != null) // if the arguments Bundle isn't empty
//		{
//			multipleContactURLEditText.setText(argumentsBundle
//					.getString(getResources().getString(
//							R.string.multiple_contact_url_dialog_bundle_key)));
//			singleContactURLEditText.setText(argumentsBundle
//					.getString(getResources().getString(
//							R.string.single_contact_url_dialog_bundle_key)));
//		} // end if

		// set the DialogFragment's title
		getDialog().setTitle(R.string.prefs_dialog_title);

		// initialize the positive Button
		Button okButton = (Button) rootView.findViewById(R.id.save_URLs_Button);
		okButton.setOnClickListener(this);
		return rootView; // return the Fragment's root View
	} // end method onCreateView

	// save this DialogFragment's state
	@Override
	public void onSaveInstanceState(Bundle argumentsBundle) {
		// add the EditText's text to the arguments Bundle
		argumentsBundle.putCharSequence(
				getResources().getString(
						R.string.multiple_contact_url_dialog_bundle_key),
				multipleContactURLEditText.getText().toString());
		argumentsBundle.putCharSequence(
				getResources().getString(
						R.string.single_contact_url_dialog_bundle_key),
				singleContactURLEditText.getText().toString());
		super.onSaveInstanceState(argumentsBundle);
	} // end method onSaveInstanceState

	@Override
	public void onClick(View clickedView) {
		if (clickedView.getId() == R.id.save_URLs_Button) {
			PrefsFragmentDialogFinishedListener listener = (PrefsFragmentDialogFinishedListener) getActivity();
			listener.onDialogFinished(multipleContactURLEditText.getText()
					.toString(), singleContactURLEditText.getText().toString());
			dismiss(); // dismiss the DialogFragment
		} // end if }
	}

}
