package net.aohayo.dotdash.morse;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class CodeSheetPagerAdapter extends FragmentPagerAdapter {
    private static final int NB_TABS = 3;

    public CodeSheetPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return new CodeSheetTabFragment();
        /*
        switch (position) {
            case 0:
                CodeSheetTabFragment tab1 = new CodeSheetTabFragment();
                return tab1;
            case 1:
                CodeSheetTabFragment tab2 = new CodeSheetTabFragment();
                return tab2;
            case 2:
                CodeSheetTabFragment tab3 = new CodeSheetTabFragment();
                return tab3;
            default:
                return null;
        }
        */
    }

    @Override
    public int getCount() {
        return NB_TABS;
    }
}
