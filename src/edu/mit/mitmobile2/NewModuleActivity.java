package edu.mit.mitmobile2;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import edu.mit.mitmobile2.MITTitleBar.OnMITTitleBarListener;

public abstract class NewModuleActivity extends ModuleActivity {

	private MITTitleBar mTitleBar;
	
	protected abstract NewModule getNewModule();
	protected abstract boolean isScrollable();
	protected abstract void onOptionSelected(String optionId);
	
	protected abstract View createSubTitleBar(); 
	
	/**
	 * Use it to add TitleBar items by use {@link NewModuleActivity#addPrimaryMenuItem(List)
	 * , NewModuleActivity#addSecondaryMenuItem(List)}}
	 */
	protected void initTitleBar() {
		List<MITMenuItem> primaryItems = getPrimaryMenuItems();
		if (primaryItems != null) {
			for (MITMenuItem item : primaryItems) {
				mTitleBar.addPrimaryItem(item);
			}
		}
		List<MITMenuItem> secondaryItems = getSecondaryMenuItems();
		if (secondaryItems != null) {
			for (MITMenuItem item : secondaryItems) {
				mTitleBar.addPrimaryItem(item);
			}
		}
	}

	// default implementation for primary, and secondary menu items.
	protected List<MITMenuItem> getPrimaryMenuItems() {
		return getNewModule().getPrimaryOptions();
	}
	
	protected List<MITMenuItem> getSecondaryMenuItems() {
		return null;
	}

	
	@Override
	public void setContentView(int layoutResID) {
		// TODO Auto-generated method stub
		super.setContentView(R.layout.new_module_main);
		LinearLayout mainLayout = (LinearLayout) findViewById(R.id.newModuleMain);
		mTitleBar = (MITTitleBar) findViewById(R.id.mitTitleBar);
		initViews();
		initTitleBar();
		mainLayout.addView(createSubTitleBar());
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(layoutResID, null);
		mainLayout.addView(view);
	}
	
	private void initViews() {
		mTitleBar.setOnTitleBarListener(new OnMITTitleBarListener() {
			@Override
			public void onOptionItemSelected(String optionId) {
				onOptionSelected(optionId);
			}

			@Override
			public void onHomeSelected() {
				MITNewsWidgetActivity.goHome(NewModuleActivity.this);				
			}

			@Override
			public void onModuleHomeSelected() {
				Intent intent = new Intent(NewModuleActivity.this, getModule().getModuleHomeActivity());
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		mTitleBar.setTextForModuleBtn(getNewModule().getShortName());
		mTitleBar.setClickableForModuleBtn(isModuleHomeActivity());
	}
}
