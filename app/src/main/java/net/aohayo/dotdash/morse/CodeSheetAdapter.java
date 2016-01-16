package net.aohayo.dotdash.morse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.aohayo.dotdash.R;

public class CodeSheetAdapter extends ArrayAdapter<CodePair> {
    private final Context context;

    public CodeSheetAdapter(Context context, MorseCodec codec) {
        super(context, 0, codec.getCodePairs());
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CodePair pair = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.code_sheet_item, parent, false);
        }

        TextView textViewChar = (TextView) convertView.findViewById(R.id.text_view_character);
        TextView textViewCode = (TextView) convertView.findViewById(R.id.text_view_code);

        String codeStr = "";
        MorseElement[] code = pair.getCode();
        for (MorseElement element : code) {
            codeStr += element.toString();
        }
        textViewChar.setText(pair.getCharacter().toString());
        textViewCode.setText(codeStr);

        return convertView;
    }
}
