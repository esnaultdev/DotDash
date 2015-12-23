package net.aohayo.dotdash.morse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import net.aohayo.dotdash.R;

public class CodeSheetFragment extends DialogFragment {

    private Context context;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.code_sheet, null));
        Dialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog = (AlertDialog) dialog;
                GridView listView = (GridView) alertDialog.findViewById(R.id.code_list);
                CodeSheetAdapter adapter = new CodeSheetAdapter(context, new MorseCodec(context, R.xml.morse_code_itu));
                listView.setAdapter(adapter);
                View closeButton = alertDialog.findViewById(R.id.image_view_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CodeSheetFragment.this.dismiss();
                    }
                });
            }
        });

        return dialog;
    }
}
