package p.gordenyou.geleide.activity;

import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import p.gordenyou.geleide.Common.SQLStatement;
import p.gordenyou.geleide.R;
import p.gordenyou.pdalibrary.common.CommonMethod;
import p.gordenyou.pdalibrary.listener.JDBCHelperQueryListener;
import p.gordenyou.pdalibrary.net.JDBCHelper;
import p.gordenyou.pdalibrary.view.ScannerView;
import p.gordenyou.pdalibrary.view.SpinnerView;
import p.gordenyou.pdalibrary.view.TextshowView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.gengXin)
    Button gengXin;
    @BindView(R.id.shangChuan)
    Button shangChuan;

    @BindView(R.id.gongDan)
    SpinnerView gongDan;
    @BindView(R.id.sheBei)
    SpinnerView sheBei;

    @BindView(R.id.zhanWei)
    ScannerView zhanWei;
    @BindView(R.id.wuLiao)
    ScannerView wuLiao;

    @BindView(R.id.num_zhanWei)
    TextshowView numZhanWei;
    @BindView(R.id.num_yiSao)
    TextshowView numYiSao;
    @BindView(R.id.zhengQue)
    TextshowView zhengQue;

    @BindView(R.id.chaZhao)
    CheckBox chaZhao;
    @BindView(R.id.shunXu)
    CheckBox shunXu;

    private long firstClick;

    private static ArrayList<HashMap<String, Object>> data;
    private LinkedHashSet<String> list_gongDan = new LinkedHashSet<>();
    private LinkedHashSet<String> list_sheBei = new LinkedHashSet<>();
    private LinkedHashMap<String, String> map_shebei = new LinkedHashMap<>();

    private int num_zhanwei;
    private int num_yisao = 0;
    private HashSet<String> set_yisao = new HashSet<>();

    private String testWuliao;

    private ArrayList<String> sqls = new ArrayList<>();

    private int preGongdan;
    private int preShebei;

    private boolean isChazhao = false;
    private boolean isShunxu = false;
    private Object[] list_key;
    private int i;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void dealData() {
        list_sheBei.clear();
        list_sheBei.add("请选择设备");
        sheBei.setSpinnerList(MainActivity.this, list_sheBei.toArray());
        getData();
    }

    private void getData() {
        JDBCHelper.getQueryHelper(SQLStatement.getGongdan(), "data", new JDBCHelperQueryListener() {

            @Override
            public void success(ArrayList<HashMap<String, Object>> result, String queryFlag) {
                data = result;

                Iterator<HashMap<String, Object>> iterator = result.iterator();
                list_gongDan.clear();
                list_gongDan.add("请选择工单");
                while (iterator.hasNext()) {
                    list_gongDan.add(iterator.next().get("gongdan").toString());
                }

                gongDan.setSpinnerList(MainActivity.this, list_gongDan.toArray());
            }

            @Override
            public void fail(String error) {
                CommonMethod.showErrorDialog(MainActivity.this, "工单更新失败，请检查网络" + error);
            }
        }).sqlQuery();


    }

    @Override
    public void logicController() {


        gengXin.setOnClickListener((view) -> {
            if (num_yisao == num_zhanwei) {
                getData();
            } else {
                CommonMethod.showErrorDialog(MainActivity.this, "当前设备还有 " +
                        (num_zhanwei - num_yisao) + " 个站位没有扫描，请勿刷新工单！");
            }
        });

        shangChuan.setOnClickListener((view) -> saveData());

        gongDan.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    if (sqls.isEmpty()) {
                        Iterator<HashMap<String, Object>> iterator = data.iterator();
                        list_sheBei.clear();
                        list_sheBei.add("请选择设备");
                        while (iterator.hasNext()) {
                            HashMap<String, Object> temp = iterator.next();
                            if (temp.get("gongdan").toString().equals(gongDan.getText())) {
                                list_sheBei.add(temp.get("shebei").toString());
                            }
                        }

                        sheBei.setSpinnerList(MainActivity.this, list_sheBei.toArray());
                        preGongdan = position;
                        preShebei = 0;
                    } else {
                        if(preGongdan != position){
                            CommonMethod.showErrorDialog(MainActivity.this, "当前设备还有 " +
                                    (num_zhanwei - num_yisao) + " 个站位没有扫描，请勿切换工单！");
                            gongDan.getSpinner().setSelection(preGongdan);
                        }
                    }
                }else{
                    if(preGongdan != 0){
                        gongDan.getSpinner().setSelection(preGongdan);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        sheBei.getSpinner().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (gongDan.getSpinner().getSelectedItemId() != 0) {
                    if(position != 0) {
                        if (sqls.isEmpty()) {
                            Iterator<HashMap<String, Object>> iterator = data.iterator();
                            map_shebei.clear();
                            while (iterator.hasNext()) {
                                HashMap<String, Object> temp = iterator.next();
                                if (temp.get("gongdan").toString().equals(gongDan.getText()) &&
                                        temp.get("shebei").toString().equals(sheBei.getText())) {
                                    map_shebei.put(temp.get("zhanwei").toString(), temp.get("wuliao").toString());
                                }
                            }

                            num_zhanwei = map_shebei.size();

                            numZhanWei.setText(String.valueOf(num_zhanwei));
                            numYiSao.setText(String.valueOf(num_yisao));

                            preShebei = position;
                        } else {
                            if(preShebei != position){
                                CommonMethod.showErrorDialog(MainActivity.this, "当前设备还有 " +
                                        (num_zhanwei - num_yisao) + " 个站位没有扫描，请勿切换设备！");
                                sheBei.getSpinner().setSelection(preShebei);
                            }

                        }
                    }
                    else{
                        if(preShebei != 0){
                            sheBei.getSpinner().setSelection(preShebei);
                        }
                    }
                } else {
                    CommonMethod.showErrorDialog(MainActivity.this, "请先选择工单！");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        zhanWei.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if(sheBei.getSpinner().getSelectedItemId() != 0){
                if (!zhanWei.getText().isEmpty()) {
                    if(map_shebei.get(zhanWei.getText()) != null){
                        testWuliao = map_shebei.get(zhanWei.getText());
                        zhengQue.setText(testWuliao);
                    }else{
                        zhanWei.setText("");
                        CommonMethod.showErrorDialog(MainActivity.this, "站位不存在！");
                    }

                }

            }else{
                CommonMethod.showErrorDialog(MainActivity.this, "请先选择设备编号！");
            }

            return false;
        });

        wuLiao.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            if (!wuLiao.getText().isEmpty()) {
                if(isChazhao){
                    if(CommonMethod.getKey(map_shebei, wuLiao.getText()).size() != 0){
                        CommonMethod.showDialog(MainActivity.this, "条码: " +
                                wuLiao.getText() + " 对应站位为： "
                                + CommonMethod.getKey(map_shebei, wuLiao.getText()).get(0));
                    }else{
                        CommonMethod.showErrorDialog(MainActivity.this, "经查询此条码没有对应站位！");
                        wuLiao.setText("");
                    }
                }
                if (wuLiao.getText().equals(testWuliao) && !set_yisao.contains(testWuliao)) {

                    if(isShunxu && i < list_key.length){
                        zhanWei.setText(list_key[i].toString());
                        zhengQue.setText(map_shebei.get(list_key[i].toString()));
                        testWuliao = map_shebei.get(list_key[i].toString());
                        i++;
                    }
                    num_yisao++;
                    numYiSao.setText(String.valueOf(num_yisao));
                    set_yisao.add(testWuliao);

                    saveSqls();

                    zhanWei.setText("");
                    wuLiao.setText("");
                    zhengQue.setText("");

                    if (num_yisao == num_zhanwei) {
                        CommonMethod.showRightDialog(MainActivity.this, "当前生产单扫描完毕，请切换生产单!");
                        gongDan.getSpinner().setSelection(0);
                        list_sheBei.clear();
                        list_sheBei.add("请选择设备");
                        sheBei.setSpinnerList(MainActivity.this, list_sheBei.toArray());

//                        zhanWei.setText("");
//                        wuLiao.setText("");

                        num_zhanwei = 0;
                        num_yisao = 0;
                        zhanWei.setText(String.valueOf(num_zhanwei));
                        zhanWei.setText(String.valueOf(num_yisao));


                    }
                } else {
                    CommonMethod.showErrorDialog(MainActivity.this, "上料错误！");
                }
            }
            return false;
        });

        chaZhao.setOnClickListener(v -> {
            if(chaZhao.isChecked()){
                isChazhao = true;
                zhanWei.getEditText().setFocusable(false);
                wuLiao.requestFocus();
            }else{
                zhanWei.getEditText().setFocusable(true);
                zhanWei.requestFocus();
            }
        });

        shunXu.setOnClickListener(v->{
            if(shunXu.isChecked()){
                isShunxu = true;
                zhanWei.getEditText().setFocusable(false);
                list_key = map_shebei.keySet().toArray();
                i = 0;
                zhanWei.setText(list_key[i].toString());
                zhengQue.setText(map_shebei.get(list_key[i].toString()));
                testWuliao = map_shebei.get(list_key[i].toString());
                i++;
                wuLiao.requestFocus();

            }else{
                zhanWei.getEditText().setFocusable(true);
                zhanWei.requestFocus();
            }
        });

    }

    private void saveSqls() {
        sqls.add(SQLStatement.getInsert(gongDan.getText(), sheBei.getText(), zhanWei.getText(), wuLiao.getText()));
    }

    private void saveData() {
        String[] str_sqls = sqls.toArray(new String[0]);
        JDBCHelper.getExecuteHelper(str_sqls, 1, (result, errorMessage) -> {
            if (result) {
                CommonMethod.showRightDialog(MainActivity.this, "数据上传成功！");
                sqls.clear();
            } else {
                CommonMethod.showErrorDialog(MainActivity.this, "数据上传失败！请检查网络！\n" + "错误信息：" + errorMessage);
            }
        }).sqlExecute();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - firstClick > 2000) {
                firstClick = System.currentTimeMillis();
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            } else {
                if (sqls.isEmpty()) {
                    System.exit(0);
                } else {
                    CommonMethod.showErrorDialog(MainActivity.this, "还有数据未上传！请上传扫描数据！");
                }

            }
            return true;
        }
        return false;
    }


    private class SBInfo {
        String zhanwei;
        String wuliao;

        public SBInfo(String zhanwei, String wuliao) {
            this.zhanwei = zhanwei;
            this.wuliao = wuliao;
        }

        public String getZhanwei() {
            return zhanwei;
        }

        public String getWuliao() {
            return wuliao;
        }
    }
}