package net.aohayo.dotdash.morse;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.aohayo.dotdash.R;

public class CodeSheetFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.code_sheet, container);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(getActivity().getResources().getString(R.string.code_sheet));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(getActivity().getResources().getDrawable(R.mipmap.ic_close_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.code_sheet_letters)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.code_sheet_numbers)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.code_sheet_punctuation)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabTextColors(Color.WHITE, Color.WHITE);

        final ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        CodeSheetPagerAdapter pagerAdapter = new CodeSheetPagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                dismiss();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
