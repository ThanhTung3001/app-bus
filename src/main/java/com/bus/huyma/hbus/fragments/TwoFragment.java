package com.bus.huyma.hbus.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bus.huyma.hbus.R;
import com.bus.huyma.hbus.activity.Adapter;
import com.bus.huyma.hbus.activity.BusRouteActivity;
import com.bus.huyma.hbus.activity.bus;

import java.util.ArrayList;
import java.util.List;

public class TwoFragment extends Fragment {
    ArrayList<bus> listData = new ArrayList<>();
    public TwoFragment (){}
    private ListView list;
    Adapter adt;
    ImageButton btnXoa;
    EditText edTimKiem;
    TextView txtMaSo;
    private static View root2;
    Button bttest;
    private final int REQ_SPEECH_INPUT = 110;

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//    }

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        listData = getData();
        adt = new Adapter(getActivity(),listData);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        super.onCreateView(inflater, container, saveInstanceState);

//        if(root2==null){
        root2 = inflater.inflate(com.bus.huyma.hbus.R.layout.fragment_two,container,false);
        list = (ListView) root2.findViewById(com.bus.huyma.hbus.R.id.lvData);
        btnXoa = (ImageButton) root2.findViewById(com.bus.huyma.hbus.R.id.btnClearSearch);
        edTimKiem = (EditText) root2.findViewById(com.bus.huyma.hbus.R.id.edSearch);
        txtMaSo = (TextView ) root2.findViewById(com.bus.huyma.hbus.R.id.maso);
        bttest = (Button) root2.findViewById(com.bus.huyma.hbus.R.id.bttest);
//        }
        return root2;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list.setAdapter(adt);

        //Lang nghe su kien tren nut xoa
        btnXoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edTimKiem.setText(null);
            }
        });


        //----------------------------------
        //Chi set hien thi BusRouteActivity
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position,long id){

                String maSoBus=null;
                txtMaSo = (TextView) view.findViewById(com.bus.huyma.hbus.R.id.maso);
                maSoBus = txtMaSo.getText().toString();

                Intent myIntent = new Intent(getActivity().getApplicationContext(), BusRouteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("code",maSoBus);

                myIntent.putExtra("Package",bundle);
                startActivity(myIntent);
            }
        });


        //Chuc nang filter trong edit text
        edTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<bus> temp = new ArrayList<>();//tao danh sach tam cac so khi search
                int textLength = edTimKiem.getText().length();//lay do dai cua chuoi khi nhap vao o tim kiem
                temp.clear(); // xoa cac phan tu trong ds tam

                //neu do dai cua chuoi nhap vao be hon ten cua kenh ,
                //khong phan biet chu hoa va chu thuong, neu ten cua kenh nao chua cac ki tu nhap trong edittext thi
                //add cac kenh do vao danh sach tam
                //sau do gan vao adapter, va set hien len list view cac kenh do.
                for (int i = 0; i < listData.size(); i++) {
                    if (textLength <= listData.get(i).getDodaimaso()) {
                        if (edTimKiem.getText().toString().equalsIgnoreCase((String) listData.get(i).getMaso().subSequence(0, textLength))) {
                            temp.add(listData.get(i));
                        }
                    }
                }
                list.setAdapter(new Adapter(getActivity().getApplicationContext(), temp));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Voice search
        bttest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkvoicedevice();
                speakvoice();
            }

        });
    }

    public void checkvoicedevice()
    {
        PackageManager pkm = getActivity().getPackageManager();
        List<ResolveInfo> act = pkm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (act.size()==0)
        {
            Toast.makeText(getActivity().getApplicationContext(),"Thi???t b??? kh??ng h??? tr???", Toast.LENGTH_SHORT).show();
        }



    }
    public void speakvoice()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        Intent intent1 = intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");
        //intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.speech_prompt));
        //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        try{
            startActivityForResult(intent,REQ_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity().getApplicationContext(), "Your device is not supported!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_SPEECH_INPUT: {

                if (resultCode == Activity.RESULT_OK  && null != data) {
                    //Tao bien chuoi de lay thong tin vua noi
                    ArrayList<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //tviewSpeak.setText(textMatchList.get(0));
                    ArrayList<bus> tam = new ArrayList<>();
                    tam.clear();
                    //String test = edtest.getText().toString();
                    String test = textMatchList.get(0);
                    //int lg= edtest.getText().length();
                    int t = test.lastIndexOf(' ');
                    //String testt=t+"";

                    test = test.substring(t+1);
                    //Toast.makeText(getActivity().getApplicationContext(),testt,Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < listData.size(); i++) {
                        if (test.equalsIgnoreCase((String) listData.get(i).getMaso()))
                        {
                            tam.add(listData.get(i));
                        }
                    }
                    list.setAdapter(new Adapter(getActivity().getApplicationContext(), tam));
                }
                break;
            }
        }
    }

    private ArrayList<bus> getData() {
        ArrayList<bus> list = new ArrayList<>();
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","1", "B???n Th??nh - B???n Xe Ch??? L???n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","2", "B???n Th??nh - B???n Xe Mi???n T??y"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","3", "B???n Th??nh - Th???nh L???c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","4", "B???n Th??nh - C???ng H??a - An S????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","5", "B???n Xe Ch??? L???n - Bi??n H??a"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","6", "B???n Xe Ch??? L???n - ?????i H???c N??ng L??m"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","7", "B???n Xe Ch??? L???n - G?? V???p"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","8", "B???n Xe Qu???n 8 - ?????i H???c Qu???c Gia"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","9", "B???n Xe Ch??? L???n - B??nh Ch??nh - H??ng Long"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","10", "?????i H???c Qu???c Gia - B???n Xe Mi???n T??y"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","100", "B???n Xe C??? Chi - C???u T??n Th??i"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","101", "B???n Xe Ch??? L???n - Ch??? T??m Nh???t"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","102", "B???n Th??nh - Nguy???n V??n Linh - B???n xe Mi???n T??y"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","103", "B???n xe Ch??? L???n - B???n xe Ng?? 4 Ga"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","104", "B???n xe An S????ng - ?????i h???c N??ng L??m"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","107", "B???n xe C??? Chi - B??? Heo"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","109", "C??ng vi??n 23/9 ??? S??n bay T??n S??n Nh???t"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","11", "B???n Th??nh ??? ?????m Sen"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","110", "Ph?? Xu??n ??? Hi???p Ph?????c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","12", "B???n Th??nh ??? Th??c Giang ??i???n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","122", "B???n xe An S????ng - T??n Quy"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","123", "Ph?? M??? H??ng (khu H) - Qu???n 1"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","124", "Ph?? M??? H??ng (khu S) - Qu???n 1"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","126", "B???n xe C??? Chi - B??nh M???"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","127", "An Th???i ????ng ??? Ng?? ba B?? X??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","128", "T??n ??i???n ??? An Ngh??a"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","13", "C??ng vi??n 23/9 - B???n xe C??? Chi"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","139", "B???n xe Mi???n T??y - Khu t??i ?????nh c?? Ph?? M???"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","14", "B???n xe Mi???n ????ng - 3/2 - B???n xe Mi???n T??y"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","140", "C??ng vi??n 23/9 - Ph???m Th??? Hi???n - Ba T??"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","141", "KDL BCR - Long Tr?????ng - KCX Linh Trung 2"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","144", "B???n xe Mi???n T??y - Ch??? L???n - CV ?????m Sen - CX Nhi??u L???c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","145", "B???n xe Ch??? L???n - Ch??? Hi???p Th??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","146", "B???n xe Mi???n ????ng - Ch??? Hi???p Th??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","148", "B???n xe Mi???n T??y - G?? V???p"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","149", "C??ng vi??n 23/9 ??? Khu d??n c?? B??nh H??ng H??a B"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","15", "Ch??? Ph?? ?????nh - ?????m Sen"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","150", "B???n xe Ch??? L???n - Ng?? 3 T??n V???n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","151", "B???n xe Mi???n T??y - B???n xe An S????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","152", "Khu d??n c?? Trung S??n ??? B???n Th??nh - S??n bay T??n S??n Nh???t"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","16", "B???n xe Ch??? L???n ??? B???n xe T??n Ph??"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","17", "B???n xe Ch??? L???n - ?????i h???c S??i G??n - Khu ch??? xu???t T??n Thu???n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","18", "B???n Th??nh - Ch??? Hi???p Th??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","19", "B???n Th??nh - KCX Linh Trung - ?????i h???c Qu???c gia"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","20", "B???n Th??nh ??? Nh?? B??"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","22", "B???n xe Qu???n 8 - KCN L?? Minh Xu??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","23", "B???n xe Ch??? L???n - Ng?? 3 Gi???ng - C???u L???n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","24", "B???n xe Mi???n ????ng - H??c M??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","25", "B???n xe Qu???n 8 - KDC V??nh L???c A"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","27", "C??ng vi??n 23/9 - ??u C?? - B???n xe An S????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","28", "C??ng vi??n 23/9 - Ch??? Xu??n Th???i Th?????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","29", "Ph?? C??t L??i - Ch??? n??ng s???n Th??? ?????c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","30", "Ch??? T??n H????ng - ?????i h???c Qu???c t???"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","31", "KDC T??n Quy - B???n Th??nh - KDC B??nh L???i"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","32", "B???n xe Mi???n T??y - B???n xe Ng?? 4 Ga"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","33", "B???n xe An S????ng - Su???i Ti??n - ?????i h???c Qu???c gia"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","34", "B???n Th??nh - ?????i h???c C??ng ngh??? S??i G??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","35", "Tuy???n xe bu??t Qu???n 1 ??? Qu???n 2"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","36", "B???n Th??nh - Th???i An"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","37", "C???ng Qu???n 4 ??? Nh??n ?????c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","38", "KDC T??n Quy - B???n Th??nh - ?????m Sen"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","39", "B???n Th??nh - V?? V??n Ki???t - B???n xe Mi???n T??y"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","40", "B???n xe Mi???n ????ng - B???n xe Ng?? 4 Ga"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","41", "B???n xe Mi???n T??y-Ng?? t?? B???n X??-B???n xe An S????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","42", "Ch??? C???u Mu???i-Ch??? n??ng s???n Th??? ?????c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","43", "B???n xe Mi???n ????ng - Ph?? C??t L??i"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","44", "C???ng Qu???n 4 ??? B??nh Qu???i"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","45", "B???n xe Qu???n 8 - B???n Th??nh - B???n xe Mi???n ????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","46", "C???ng Qu???n 4 - B???n M??? C???c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","47", "B???n xe Ch??? L???n - Qu???c l??? 50 - H??ng Long"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","48", "B???n xe T??n Ph?? - Ch??? Hi???p Th??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","49", "S??n bay T??n S??n Nh???t - Qu???n 1"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","50", "?????i h???c B??ch khoa - ?????i h???c Qu???c gia"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","51", "B???n xe Mi???n ????ng - B??nh H??ng H??a"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","52", "B???n Th??nh - ?????i h???c Qu???c t???"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","53", "L?? H???ng Phong - ?????i h???c Qu???c gia"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","54", "B???n xe Mi???n ????ng - B???n xe Ch??? L???n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","55", "C??ng vi??n ph???n m???m Quang Trung - Khu C??ng ngh??? cao (Q9)"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","56", "B???n xe Ch??? L???n - ?????i h???c Giao th??ng V???n t???i"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","57", "Ch??? Ph?????c B??nh - Tr?????ng THPT Hi???p B??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","58", "B???n xe Ng?? 4 Ga - B??nh M???"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","59", "B???n xe Qu???n 8 - B???n xe Ng?? 4 Ga"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","60", "B???n xe An S????ng - KCN L?? Minh Xu??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","60-1", "BX Mi???n T??y - BX Bi??n H??a"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","60-2", "Ph?? T??c - ?????i h???c N??ng L??m"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","60-3", "B???n xe Mi???n ????ng ??? Khu C??ng nghi???p Nh??n Tr???ch"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","60-4", "B???n xe Mi???n ????ng - B???n xe H??? Nai"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","61", "B???n xe Ch??? L???n - KCN L?? Minh Xu??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","61-1", "Th??? ?????c - D?? An"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","61-3", "B???n xe An S????ng ??? Th??? D???u M???t"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","61-4", "B???n D?????c ??? D???u Ti???ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","61-6", "B???n Th??nh - Khu Du l???ch ?????i Nam"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","61-7", "B???n ???? B??nh M??? - B???n xe B??nh D????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","61-8", "B???n xe Mi???n T??y ??? Khu Du l???ch ?????i Nam"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62", "B???n xe Qu???n 8 -Th???i An"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-1", "B???n xe Ch??? L???n - B???n L???c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-10", "B???n xe Ch??? L???n - Thanh V??nh ????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-11", "B???n xe Qu???n 8 - T??n T???p"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-2", "B???n xe Ch??? L???n - Ng?? 3 T??n L??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-3", "B???n C??? Chi - B???n xe H???u Ngh??a"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-4", "Th??? tr???n T??n T??c - Ch??? B???n L???c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-5", "B???n xe An S????ng - B???n xe H???u Ngh??a"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-6", "B???n xe Ch??? L???n-B???n xe H???u Ngh??a"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-7", "B???n xe Ch??? L???n - B???n xe ?????c Hu???"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-8", "B???n xe Ch??? L???n - B???n xe T??n An"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","62-9", "B???n xe Qu???n 8 ??? C???u N???i"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","64", "B???n xe Mi???n ????ng - ?????m Sen"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","65", "B???n Th??nh - CMT8 - B???n xe An S????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","66", "B???n xe Ch??? L???n - B???n xe An S????ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","68", "B???n xe Ch??? L???n - KCX T??n Thu???n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","69", "C??ng vi??n 23/9 ??? KCN T??n B??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","70", "T??n Quy - B???n S??c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","70-1", "B???n xe C??? Chi - B???n xe G?? D???u"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","70-2", "BX C??? Chi ??? H??a Th??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","70-3", "B???n Th??nh ??? M???c B??i"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","70-5", "B??? Heo - L???c H??ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","71", "B???n xe An S????ng - Ph???t C?? ????n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","72", "C??ng vi??n 23/9 ??? Hi???p Ph?????c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","73", "Ch??? B??nh Ch??nh - KCN L?? Minh Xu??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","74", "B???n xe An S????ng - B???n xe C??? Chi"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","75", "S??i G??n - C???n Gi???"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","76", "Long Ph?????c - Su???i Ti??n - ?????n Vua H??ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","77", "?????ng H??a ??? C???n Th???nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","78", "Th???i An ??? H??c M??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","79", "B???n xe C??? Chi - ?????n B???n D?????c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","81", "B???n xe Ch??? L???n - L?? Minh Xu??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","83", "B???n xe C??? Chi - C???u Th???y Cai"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","84", "B???n xe Ch??? L???n - T??n T??c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","85", "B???n xe An S????ng ??? KCN Nh??? Xu??n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","86", "B???n Th??nh - ?????i h???c T??n ?????c Th???ng"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","87", "B???n xe C??? Chi - An Nh??n T??y"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","88", "B???n Th??nh - Ch??? Long Ph?????c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","89", "?????i h???c N??ng L??m - Tr?????ng THPT Hi???p B??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","90", "Ph?? B??nh Kh??nh - C???n Th???nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","91", "B???n xe Mi???n T??y - Ch??? n??ng s???n Th??? ?????c"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","93", "B???n Th??nh - ?????i h???c N??ng L??m"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","94", "B???n xe Ch??? L???n - B???n xe C??? Chi"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","95", "B???n xe Mi???n ????ng - KCN T??n B??nh"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","96", "B???n Th??nh - Ch??? B??nh ??i???n"));
        list.add(new bus(com.bus.huyma.hbus.R.drawable.buslogo, "Tuy???n S???","99", "Ch??? Th???nh M??? L???i - ?????i h???c Qu???c gia"));
        return list;
    }
}
