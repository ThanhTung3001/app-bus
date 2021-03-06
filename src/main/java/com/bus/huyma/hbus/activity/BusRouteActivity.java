package com.bus.huyma.hbus.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bus.huyma.hbus.R;
import com.bus.huyma.hbus.fragments.InfoBusDetail;
import com.bus.huyma.hbus.fragments.TimeBusDetail;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BusRouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Geocoder geo;
    DatabaseHelper DBHelper;
    TextView txtInfoDetail;
    private static int choiceStop = 0;//luu gia tri lua chon hien thi tram dung
    private static int choiceRoute = 0;//luu gia tri lua chon hien thi lo trinh
    private static int choiceDisplay = 1;//luu gia tri lua chon keiu hien thi
    private static int choicePlace = 0;//luu gia tri lua chon vi tri

    private static int direction =1; //chieu di xe bus
    private static boolean start_station =true;// Cho phep Focus ben xe dau tien
    private static boolean display_route =true;//Cho phep hien thi lo trinh xe bus
    private static boolean display_station =true;//Cho phep hien thi tram xe bus
    private static String codeBus = null;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_route);
        //-------------------------------------------------------------
        viewPager = (ViewPager) findViewById(R.id.viewpagerBusRoute);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabsInBusRouteLayout);
        tabLayout.setupWithViewPager(viewPager);
        //-------------------------------------------------------------

        txtInfoDetail = (TextView) findViewById(R.id.txtDetailBusRoute);
        toolbar = (Toolbar) findViewById(R.id.toolbar_BusRoute);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapBusRoute);
        mapFragment.getMapAsync(this);

        //Goi Inten lay ve cac gia tri gui tu fragment 2
        Intent callIntent = getIntent();
        Bundle getBundle = callIntent.getBundleExtra("Package");
        codeBus = getBundle.getString("code");

        //Set label moi cho activity
        setTitle("Tuy???n xe bus s??? " + codeBus);

        //Kiem tra du lieu (code-ma so xe) gui bang Bundle tu fragment 2 co trong CSDL ko.
        if(checkData()){
            //thongTinChiTiet();
        } else{
            //Xuat thong bao loi, khi ko co du lieu xe bus trong CSDL
            AlertDialog.Builder aB = new AlertDialog.Builder(this);
            aB.setTitle("OPPs. Kh??ng t??m th???y d??? li???u !!");
            aB.setIcon(R.drawable.error_database);
            aB.setMessage("Hi???n t???i d??? li???u tuy???n xe bus n??y ??ang ???????c ch??ng t??i c???p nh???t...");
            aB.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            aB.create();
            aB.show();
        }
    }

    //Ham ve lo trinh xe bus
    //Ham se callBack trong onMapReady()
    private void drawBusRoute(GoogleMap map){
        DBHelper = new DatabaseHelper(this);
        try{
            DBHelper.createDataBase();
        }catch(IOException e){
            throw new Error("Unable to create database.",e);
        }

        //Mo ket noi voi database da co san
        try{
            DBHelper.openDataBase();
        }catch (SQLException s){
            throw s;
        }

        if(direction ==1 ){
            //Truy van lay ra cac toa do tren duong di cua xe, theo chieu di, direction==1
            Cursor c = DBHelper.rawQuery("select latitude,longitude from Path_mark " +
                    "where direction='1' and bus_id in (select id from Bus where code='"+codeBus + "') order by orders asc", null);
            if(c.moveToFirst()){
                PolylineOptions pathMarkBus = new PolylineOptions();
                do{
                    if(display_route==true){
                        double x = c.getDouble(c.getColumnIndex("latitude"));//vi do
                        double y = c.getDouble(c.getColumnIndex("longitude"));//kinh do
                        pathMarkBus.add(new LatLng(x,y)).color(Color.BLACK).width(8);
                    }else if(display_route==false){
                        map.clear();
                    }
                }while(c.moveToNext());

                if(display_route==true){
                    Polyline routeBus = map.addPolyline(pathMarkBus);
                }
            }
        }
        //Ve lo trinh xe bus theo chieu ve
        else if(direction == 2){
            Cursor c1 = DBHelper.rawQuery("select latitude,longitude from Path_mark " +
                    "where direction='2' and bus_id in (select id from Bus where code='"+codeBus+"') order by orders asc", null);
            if(c1.moveToFirst()){
                PolylineOptions pathMarkBus = new PolylineOptions();
                do{
                    if(display_route==true){
                        double x = c1.getDouble(c1.getColumnIndex("latitude"));//vi do
                        double y = c1.getDouble(c1.getColumnIndex("longitude"));//kinh do
                        pathMarkBus.add(new LatLng(x,y)).color(Color.RED).width(8);
                    }else if(display_route==false){
                        map.clear();
                    }
                }while(c1.moveToNext());

                if(display_route==true){
                    Polyline routeBus = map.addPolyline(pathMarkBus);
                }
            }
        }
        //Lo trinh theo ca 2 chieu
        else if(direction == 3){
            Cursor c = DBHelper.rawQuery("select latitude,longitude from Path_mark " +
                    "where direction='1' and bus_id in (select id from Bus where code='"+codeBus+"') order by orders asc", null);
            Cursor c1 = DBHelper.rawQuery("select latitude,longitude from Path_mark " +
                    "where direction='2' and bus_id in (select id from Bus where code='"+codeBus+"') order by orders asc", null);

            //Ve lo trinh chieu di xe bus
            if(c.moveToFirst()){
                PolylineOptions pathMarkBus1 = new PolylineOptions();
                do{
                    if(display_route==true){
                        double x = c.getDouble(c.getColumnIndex("latitude"));//vi do
                        double y = c.getDouble(c.getColumnIndex("longitude"));//kinh do
                        pathMarkBus1.add(new LatLng(x,y)).color(Color.BLACK).width(8);
                    }else if(display_route==false){
                        map.clear();
                    }
                }while(c.moveToNext());

                if(display_route==true){
                    Polyline routeBus1 = map.addPolyline(pathMarkBus1);
                }
            }

            //Ve lo trinh chieu ve xe bus
            if(c1.moveToFirst()){
                PolylineOptions pathMarkBus2 = new PolylineOptions();
                do{
                    if(display_route==true){
                        double x = c1.getDouble(c1.getColumnIndex("latitude"));//vi do
                        double y = c1.getDouble(c1.getColumnIndex("longitude"));//kinh do
                        pathMarkBus2.add(new LatLng(x,y)).color(Color.RED).width(8);
                    }else if(display_route==false){
                        map.clear();
                    }
                }while(c1.moveToNext());

                if(display_route==true){
                    Polyline routeBus2 = map.addPolyline(pathMarkBus2);
                }
            }
        }
    }

    //Ham lay vi tri hien tai
    private void myLocation(GoogleMap map) {
        LocationManager locM = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria cr = new Criteria();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location loc = locM.getLastKnownLocation(locM.getBestProvider(cr, true));
        if(loc!=null){
            LatLng latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
            //map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng)
                    .zoom(15)
                    .build();

            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    //Ham ve cac tram tren duong di
    private void drawBusStop(GoogleMap map){
        DBHelper = new DatabaseHelper(this);
        try{
            DBHelper.createDataBase();
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            DBHelper.openDataBase();
        }catch(SQLException s){
            throw s;
        }

        if(direction ==1){
            //Truy van lay ra toa do cac tram xe bus theo chieu di, diretion ==1
            Cursor c = DBHelper.rawQuery("select Bus_stop.address, Bus_stop.name, Bus_stop.latitude, Bus_stop.longitude from Bus_stop " +
                    "inner join Bus_BStop on Bus_stop.id=Bus_BStop.bstop_id " +
                    "inner join Bus on Bus_BStop.bus_id=Bus.id where Bus.code='" + codeBus + "' and Bus_BStop.direction='1'", null);
            if(c.moveToFirst()){

                //Lay ra toa do tram dau tien trong luot di hoac ve, de mapAminate ve toa do do'
                double x1 = c.getDouble(c.getColumnIndex("latitude"));
                double y1 = c.getDouble(c.getColumnIndex("longitude"));
                LatLng firstStopPoint = new LatLng(x1,y1);
                LatLng lastStopPoint = new LatLng(0.0,0.0);//Khai b??o bi???n LatLng ????? l???y v??? t???a ????? c???a b???n cu???i

                //Vong lap lan luot add marker cho cac tram xe bus
                do{

                    //Neu cai dat hien vi tri cac tram dung xe bus la CO'
                    //thi se ve~ cac tram xe bus len ban do.
                    //Neu false thi se khong hien thi tram xe bus
                    if(display_station==true){
                        String tenTram = c.getString(c.getColumnIndex("name"));//ten tram
                        String diaChi = c.getString(c.getColumnIndex("address"));//dia chi tram
                        double x = c.getDouble(c.getColumnIndex("latitude"));//vi do
                        double y = c.getDouble(c.getColumnIndex("longitude"));//kinh do
                        map.addMarker(new MarkerOptions().position(new LatLng(x, y))
                                .title("Tr???m " + tenTram)
                                .snippet(diaChi)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker)));
                    } else if(display_station==false){
                        map.clear(); // N???u c??i ?????t hi???n v??? tr?? l?? false, th?? ta x??a marker t???t c??? tr???m ????
                        //Th???c ch???t l?? x??a c??? b???n ????? , ?????ng ngh?? vs vi???c m???t lu??n l??? tr??nh xe bus
                        //Nh??ng ta s??? v??? l???i l??? tr??nh b???ng c??c g???i l???i h??m drawBusRoute() trong item th??? 2 c???a c??i ?????t hi???n v??? tr?? tr???m
                    }

                    //N???u con tr??? tr??? t???i ??i???m cu???i c??ng, th?? l???y ra t???a ????? ??i???m ????
                    //????? target v??o v??? tr?? cu???i b???n
                    if(c.isLast()){
                        double x2 = c.getDouble(c.getColumnIndex("latitude"));
                        double y2 = c.getDouble(c.getColumnIndex("longitude"));
                        lastStopPoint = new LatLng(x2,y2);
                    }
                }while(c.moveToNext());

                //Chuyen camera ve vi tri tram dau tien cua lo trinh tuyen xe bus
                if(display_station==true ){
                    if(start_station==true){
                        CameraPosition cp = new CameraPosition.Builder().target(firstStopPoint).zoom(15).build();
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                    }else {
                        CameraPosition cp = new CameraPosition.Builder().target(lastStopPoint).zoom(15).build();
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                    }
                }
            }
        }
        //Ve tram xe bus theo chieu ve, direction==2
        else if(direction ==2){
            Cursor c1 = DBHelper.rawQuery("select Bus_stop.address, Bus_stop.name, Bus_stop.latitude, Bus_stop.longitude from Bus_stop " +
                    "inner join Bus_BStop on Bus_stop.id=Bus_BStop.bstop_id " +
                    "inner join Bus on Bus_BStop.bus_id=Bus.id where Bus.code='"+codeBus+"' and Bus_BStop.direction='2'", null);
            if(c1.moveToFirst()){

                //Lay ra toa do tram dau tien trong luot di hoac ve, de mapAminate ve toa do do'
                double x1 = c1.getDouble(c1.getColumnIndex("latitude"));
                double y1 = c1.getDouble(c1.getColumnIndex("longitude"));
                LatLng firstStopPoint = new LatLng(x1,y1);
                LatLng lastStopPoint = new LatLng(0.0,0.0);//Khai b??o bi???n LatLng ????? l???y v??? t???a ????? c???a b???n cu???i

                //Vong lap lan luot add marker cho cac tram xe bus
                do{

                    //Neu cai dat hien vi tri cac tram dung xe bus la CO'
                    //thi se ve~ cac tram xe bus len ban do.
                    //Neu false thi se khong hien thi tram xe bus
                    if(display_station==true){
                        String tenTram = c1.getString(c1.getColumnIndex("name"));//ten tram
                        String diaChi = c1.getString(c1.getColumnIndex("address"));//dia chi tram
                        double x = c1.getDouble(c1.getColumnIndex("latitude"));//vi do
                        double y = c1.getDouble(c1.getColumnIndex("longitude"));//kinh do
                        map.addMarker(new MarkerOptions().position(new LatLng(x, y))
                                .title("Tr???m " + tenTram)
                                .snippet(diaChi)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker_backward)));
                    } else if(display_station==false){
                        map.clear(); // N???u c??i ?????t hi???n v??? tr?? l?? false, th?? ta x??a marker t???t c??? tr???m ????
                        //Th???c ch???t l?? x??a c??? b???n ????? , ?????ng ngh?? vs vi???c m???t lu??n l??? tr??nh xe bus
                        //Nh??ng ta s??? v??? l???i l??? tr??nh b???ng c??c g???i l???i h??m drawBusRoute() trong item th??? 2 c???a c??i ?????t hi???n v??? tr?? tr???m
                    }

                    //N???u con tr??? tr??? t???i ??i???m cu???i c??ng, th?? l???y ra t???a ????? ??i???m ????
                    //????? target v??o v??? tr?? cu???i b???n
                    if(c1.isLast()){
                        double x2 = c1.getDouble(c1.getColumnIndex("latitude"));
                        double y2 = c1.getDouble(c1.getColumnIndex("longitude"));
                        lastStopPoint = new LatLng(x2,y2);
                    }

                }while(c1.moveToNext());

                //Chuyen camera ve vi tri tram dau tien cua lo trinh tuyen xe bus
                if(display_station==true){
                    if(start_station==true){
                        CameraPosition cp = new CameraPosition.Builder().target(firstStopPoint).zoom(15).build();
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                    }else {
                        CameraPosition cp = new CameraPosition.Builder().target(lastStopPoint).zoom(15).build();
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                    }
                }
            }
        }
        //Ve tram xe bus ca 2 chieu
        else if(direction == 3){
            Cursor c = DBHelper.rawQuery("select Bus_stop.address, Bus_stop.name, Bus_stop.latitude, Bus_stop.longitude from Bus_stop " +
                    "inner join Bus_BStop on Bus_stop.id=Bus_BStop.bstop_id " +
                    "inner join Bus on Bus_BStop.bus_id=Bus.id where Bus.code='"+codeBus+"' and Bus_BStop.direction='1'", null);

            Cursor c1 = DBHelper.rawQuery("select Bus_stop.address, Bus_stop.name, Bus_stop.latitude, Bus_stop.longitude from Bus_stop " +
                    "inner join Bus_BStop on Bus_stop.id=Bus_BStop.bstop_id " +
                    "inner join Bus on Bus_BStop.bus_id=Bus.id where Bus.code='"+codeBus+"' and Bus_BStop.direction='2'", null);

            if(c.moveToFirst()){

                //Lay ra toa do tram dau tien trong luot di hoac ve, de mapAminate ve toa do do'
                double x1 = c.getDouble(c.getColumnIndex("latitude"));
                double y1 = c.getDouble(c.getColumnIndex("longitude"));
                LatLng firstStopPoint = new LatLng(x1,y1);

                //Vong lap lan luot add marker cho cac tram xe bus
                do{

                    //Neu cai dat hien vi tri cac tram dung xe bus la CO'
                    //thi se ve~ cac tram xe bus len ban do.
                    //Neu false thi se khong hien thi tram xe bus
                    if(display_station==true){
                        String tenTram = c.getString(c.getColumnIndex("name"));//ten tram
                        String diaChi = c.getString(c.getColumnIndex("address"));//dia chi tram
                        double x = c.getDouble(c.getColumnIndex("latitude"));//vi do
                        double y = c.getDouble(c.getColumnIndex("longitude"));//kinh do
                        map.addMarker(new MarkerOptions().position(new LatLng(x, y))
                                .title("Tr???m " + tenTram)
                                .snippet(diaChi)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker)));
                    } else if(display_station==false){
                        map.clear(); // N???u c??i ?????t hi???n v??? tr?? l?? false, th?? ta x??a marker t???t c??? tr???m ????
                        //Th???c ch???t l?? x??a c??? b???n ????? , ?????ng ngh?? vs vi???c m???t lu??n l??? tr??nh xe bus
                        //Nh??ng ta s??? v??? l???i l??? tr??nh b???ng c??c g???i l???i h??m drawBusRoute() trong item th??? 2 c???a c??i ?????t hi???n v??? tr?? tr???m
                    }
                }while(c.moveToNext());

                //Chuyen camera ve vi tri tram dau tien cua lo trinh tuyen xe bus
                if(display_station==true){
                    CameraPosition cp = new CameraPosition.Builder().target(firstStopPoint).zoom(15).build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                }
            }

            if(c1.moveToFirst()){

                //Lay ra toa do tram dau tien trong luot di hoac ve, de mapAminate ve toa do do'
                double x1 = c1.getDouble(c1.getColumnIndex("latitude"));
                double y1 = c1.getDouble(c1.getColumnIndex("longitude"));
                LatLng firstStopPoint = new LatLng(x1,y1);

                //Vong lap lan luot add marker cho cac tram xe bus
                do{

                    //Neu cai dat hien vi tri cac tram dung xe bus la CO'
                    //thi se ve~ cac tram xe bus len ban do.
                    //Neu false thi se khong hien thi tram xe bus
                    if(display_station==true){
                        String tenTram = c1.getString(c1.getColumnIndex("name"));//ten tram
                        String diaChi = c1.getString(c1.getColumnIndex("address"));//dia chi tram
                        double x = c1.getDouble(c1.getColumnIndex("latitude"));//vi do
                        double y = c1.getDouble(c1.getColumnIndex("longitude"));//kinh do
                        map.addMarker(new MarkerOptions().position(new LatLng(x, y))
                                .title("Tr???m " + tenTram)
                                .snippet(diaChi)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker_backward)));
                    } else if(display_station==false){
                        map.clear(); // N???u c??i ?????t hi???n v??? tr?? l?? false, th?? ta x??a marker t???t c??? tr???m ????
                        //Th???c ch???t l?? x??a c??? b???n ????? , ?????ng ngh?? vs vi???c m???t lu??n l??? tr??nh xe bus
                        //Nh??ng ta s??? v??? l???i l??? tr??nh b???ng c??c g???i l???i h??m drawBusRoute() trong item th??? 2 c???a c??i ?????t hi???n v??? tr?? tr???m
                    }
                }while(c1.moveToNext());

                //Chuyen camera ve vi tri tram dau tien cua lo trinh tuyen xe bus
                /*if(display_station==true){
                    CameraPosition cp = new CameraPosition.Builder().target(firstStopPoint).zoom(15).build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                } */
            }
        }
    }

    //Ham kiem tra du lieu xe bus trong CSDL SQLite
    //Neu chua co du lieu tuyen xe bus do thi se xuat ra thong bao
    private boolean checkData(){

        boolean check=true;//khoi tao bien kiem tra la true
        DBHelper = new DatabaseHelper(this);
        try{
            DBHelper.createDataBase();
        }catch(IOException e){
            throw new Error("Unable to create database.",e);
        }

        //Mo ket noi voi database da co san
        try{
            DBHelper.openDataBase();
        }catch (SQLException s){
            throw s;
        }

        //Truy van lay ve danh sach ma so cac xe bus
        //Dem so sanh voi du lieu (code-maso xe bus) gui tu fragment 2
        //Neu code co trong CSDL, tuc la co thong tin xe bus, roi tra ve true, ngc lai tra ve false
        Cursor c = DBHelper.rawQuery("select code from Bus",null);
        if(c.moveToFirst()){

            //Tao vong lap,dung cusor cho chay tu phan tu dau tien den het,
            // neu codeBus trung gia tri cua thuoc tinh code trong CSDL thi gan check=true,va break ra,va return = check(luc nay la true)
            //Neu codeBus ko trung voi bat ki gia tri nao cua thuoc tinh code(trong bang Bus-SQLite) thi ta gan check=false, va xet cursor tiep theo
            //Den het ma ko tim ra duoc gia tri nao trugn gia tri codeBus , thi return check ve false.
            do{
                String maSoXe = c.getString(c.getColumnIndex("code"));
                if(codeBus.equals(maSoXe)){
                    check=true;
                    break;
                } else {
                    check=false;
                }
            }while (c.moveToNext());
        }
        return check;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        geo = new Geocoder(this, Locale.getDefault());

        myLocation(mMap);

        if(checkData()){
            drawBusRoute(mMap);//ve duong di cua tuyen xe bus
            drawBusStop(mMap);//ve cac tuyen xe bus tren duong di
        } else  mMap.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_bus_route, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.itemHienThiTramDung: {
                final String[] luaChonYesNo = getResources().getStringArray(R.array.settingHienThi);//Cac item lua chon se xuat hien trong dialog
                AlertDialog.Builder aB = new AlertDialog.Builder(this)
                        .setSingleChoiceItems(luaChonYesNo,choiceStop ,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Item th??? nh???t d??ng ????? hi???n th??? marker c??c tr???m
                                if(which==0){
                                    choiceStop=0;
                                    display_station = true;
                                    drawBusStop(mMap);//Ch???y qua h??m n??y, ??i v??o l???nh if(display_station = true), v?? add l???n l?????t c??c marker
                                    dialog.cancel();
                                } else if(which==1){
                                    choiceStop=1;
                                    display_station = false;
                                    drawBusStop(mMap);//??i v??o l???nh false, x??a c??? b???n ?????, bao g???m tr???m v?? l??? tr??nh
                                    drawBusRoute(mMap);// Sau ???? v??? l???i l??? tr??nh, t???o c???m gi??c nh?? ch??? x??a c??c tr???m
                                    dialog.cancel();
                                }
                            }
                        })
                        .setNegativeButton("H???y", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                //set title
                aB.setTitle("Thi???t l???p hi???n th??? tr???m d???ng xe bu??t tr??n b???n ?????.");
                aB.create();
                aB.show();
                return true;
            }

            case R.id.itemHienThiLoTrinh: {
                String[] luaChonYesNo = getResources().getStringArray(R.array.settingHienThi);//Cac item lua chon se xuat hien trong dialog
                AlertDialog.Builder aB = new AlertDialog.Builder(this);
                aB.setTitle("Hi???n th??? l??? tr??nh c???a xe bu??t.");
                aB.setSingleChoiceItems(luaChonYesNo, choiceRoute,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            choiceRoute=0;
                            display_route=true;
                            drawBusRoute(mMap);//Cai dat hien thi la true, ve lo trinh xe bus
                            dialog.cancel();
                        }else if(which==1){
                            choiceRoute=1;
                            display_route=false;
                            drawBusRoute(mMap);
                            drawBusStop(mMap);
                            dialog.cancel();
                        }
                    }
                })
                        .setNegativeButton("H???y", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                aB.create();
                aB.show();
                return true;
            }

            case R.id.itemKieuHienThi:{
                String[] luachonKieuHienThi = getResources().getStringArray(R.array.settingKieuHienThi);//Cac lua chon se xuat hien trong Dialog kieu hien thi
                AlertDialog.Builder aB = new AlertDialog.Builder(this);
                aB.setTitle("Ch???n m???t trong nh???ng ki???u hi???n th???.");
                aB.setSingleChoiceItems(luachonKieuHienThi, choiceDisplay,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //??i???u ki???n ch???n hi???n th??? c??? 2 l?????t ??i
                        //V???i t??y ch???n ban ?????u, which ==0
                        if(which==0){
                            choiceDisplay=0;
                            //??i v??o v??ng l???p true , v??? ra t???t c??? c??c tr???m v?? l??? tr??nh
                            direction =3;
                            callBackViewPager();
                            //??i???u ki???n if(direction==3 ) v?? display_route,display_station s??? b???ng true
                            drawBusRoute(mMap);
                            drawBusStop(mMap);
                            dialog.cancel();
                        }
                        //V???i t??y ch???n th??? 2, which ==1, v??? ra l?????t ??i c???a xe bus
                        else if(which==1){
                            choiceDisplay=1;
                            direction=1;
                            callBackViewPager();
                            //Ti???n h??nh x??a map v?? v??? l???i l?????t ??i
                            mMap.clear();

                            //??i???u ki???n if(direction==1 ) v?? display_route,display_station s??? b???ng true
                            drawBusRoute(mMap);
                            drawBusStop(mMap);
                            dialog.cancel();
                        }
                        //V???i t??y ch???n th??? 3, which ==2, v??? ra l?????t v???
                        else if(which==2){
                            choiceDisplay=2;
                            direction=2;
                            callBackViewPager();
                            //TI???n h??nh x??a map, v?? v??? l???i l?????t v???
                            mMap.clear();

                            //??i???u ki???n if(direction==2 ) v?? display_route,display_station s??? b???ng true
                            drawBusRoute(mMap);
                            drawBusStop(mMap);
                            dialog.cancel();
                        }
                    }
                })
                        .setNegativeButton("H???y", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                aB.create();
                aB.show();
                return true;
            }

            case R.id.itemChonViTri:{
                String[] luachonChonViTri = getResources().getStringArray(R.array.settingChonViTri);
                AlertDialog.Builder aB= new AlertDialog.Builder(this);
                aB.setTitle("M???c ?????nh hi???n th??? v??? tr?? ?????u b???n ho???c cu???i b???n.");
                aB.setSingleChoiceItems(luachonChonViTri, choicePlace, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            choicePlace=0;
                            start_station=true;
                            drawBusStop(mMap);
                            dialog.cancel();
                        }else if(which == 1){
                            choicePlace=1;
                            start_station=false;
                            drawBusStop(mMap);
                            dialog.cancel();
                        }
                    }
                })
                        .setNegativeButton("H???y", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                aB.create();
                aB.show();
                return true;
            }
            case android.R.id.home: finish(); return true;
            default:return  super.onOptionsItemSelected(item);
        }
    }

    //--------------------------------------------------
    //Tra ve du lieu check (true or false) qua fragment de kiem tra co du lieu xe bus hay ko
    //De hien thi thong tin xe bus
    //Neu check=true thi fragment se tien hanh truy van csdl theo ma so xe bus, va lay ra thong tin chi tiet
    //Nguoc lai se khong lay duoc thong tin chi tiet xe bus va thong bao cho nguoi dung biet
    public String sendDataToFragment(){
        String check=null;
        if(checkData()){
            check="true";
        } else check="false";

        return check;
    }

    //Ham tra ve ma so xe bus (codeBus-cai duoc gui tu item listView, khi ta chon 1 xe bat ki)
    //Fragment se lay gia tri ma so xe bus nay ,ket hop voi dieu kien check o tren de truy van csdl va lay ra thong tin xe bus
    public String sendCodeBusToFragment(){
        return codeBus;
    }

    //Ham tra ve chieu di, hay chieu ve cua xe bus, gui qua fragment 'TimeBusDetail' de truy van csdl
    //va lay ra thong tin thoi gian cua luot di hay luot ve
    public int sendDirectionBusToFragment(){
        return direction;
    }

    //--------------------------------------------------
    //Ham View Pager
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new InfoBusDetail(), "Th??ng tin");
        adapter.addFragment(new TimeBusDetail(), "Bi???u ????? gi???");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void callBackViewPager(){
        sendDirectionBusToFragment();
        setupViewPager(viewPager);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
