package br.org.sidia.eva.mainview;

import android.view.View;

import br.org.sidia.eva.view.IView;

public interface ICleanView extends IView {
    void setOnCancelClickListener(View.OnClickListener listener);

    void setOnConfirmClickListener(View.OnClickListener listener);
}
