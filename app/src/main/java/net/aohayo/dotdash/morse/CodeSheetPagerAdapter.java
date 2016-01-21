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
        switch (position) {
            case 0:
                return CodeSheetTabFragment.newInstance(CodeType.LETTER);
            case 1:
                return CodeSheetTabFragment.newInstance(CodeType.NUMBER);
            case 2:
                return CodeSheetTabFragment.newInstance(CodeType.PUNCTUATION);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NB_TABS;
    }
}
