package za.nmu.wrpv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.barteksc.pdfviewer.PDFView;

import za.nmu.wrpv.qwirkle.R;

public class JoinActivity extends AppCompatActivity {

    AppCompatButton joinBtn;
    EditText  name,ip;

    PDFView pdfView;

    ImageView info,logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        joinBtn=findViewById(R.id.join_btn);
        name=findViewById(R.id.name);
        ip=findViewById(R.id.ip);
        logo=findViewById(R.id.logo);

        YoYo.with(Techniques.RollIn)
                .duration(2000)
                .playOn(logo);

        YoYo.with(Techniques.RubberBand)
                .delay(2000)
                .duration(2000)
                .playOn(logo);

        pdfView=findViewById(R.id.pdfView_join);
        info=findViewById(R.id.info_join_btn);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pdfView.getVisibility() == View.VISIBLE) {

                    pdfView.setVisibility(View.GONE);
                    joinBtn.setVisibility(View.VISIBLE);
                } else
                {
                    joinBtn.setVisibility(View.GONE);
                    pdfView.setVisibility(View.VISIBLE);

                    setUpPdf();
                }

            }
        });



    }


    public void join(View view) {

        String nameValue,ipValue;
        nameValue=name.getText().toString();
        ipValue=ip.getText().toString();


        Intent intent= new Intent(this, MainActivity.class);
        intent.putExtra("name",nameValue);
        intent.putExtra("ip",ipValue);
        startActivity(intent);
        finish();

    }

    private void setUpPdf() {

        pdfView.fromAsset("qwirkle_rules.pdf")
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(0)
                .load();
    }

}