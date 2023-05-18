package com.gachon.innergation.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.gachon.innergation.R;
public class CustomDialog extends Dialog {
    public CustomDialog(Context context){
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_process);
    }
}
