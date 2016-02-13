package net.aohayo.dotdash.morse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import net.aohayo.dotdash.R;

public class CodeSheetTabFragment extends Fragment {
    private static final String CODE_TYPE = "";

    public static CodeSheetTabFragment newInstance(CodeType type) {
        CodeSheetTabFragment fragment = new CodeSheetTabFragment();

        Bundle args = new Bundle();
        args.putSerializable(CODE_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.code_sheet_tab, container, false);

        CodeType type = (CodeType) getArguments().getSerializable(CODE_TYPE);
        if (type == null) {
            type = CodeType.LETTER;
        }

        MorseCodec codec = MorseCodec.getInstance();
        if (!codec.isInit()) {
            codec.init(getActivity());
        }

        GridView listView = (GridView) view.findViewById(R.id.code_list);
        CodeSheetAdapter adapter = new CodeSheetAdapter(getActivity(), codec, type);
        listView.setAdapter(adapter);

        return view;
    }
}
