package net.aohayo.dotdash.morse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import net.aohayo.dotdash.R;

public class CodeSheetTabFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.code_sheet_tab, container, false);

        /*
        MorseCodec codec = MorseCodec.getInstance();
        if (!codec.isInit()) {
            codec.init(context, R.xml.morse_code_itu);
        }

        GridView listViewPunc = (GridView) alertDialog.findViewById(R.id.code_list_punctuation);
        CodeSheetAdapter adapterPunc = new CodeSheetAdapter(context, codec, CodeType.PUNCTUATION);
        listViewPunc.setAdapter(adapterPunc);
        */
    }
}
