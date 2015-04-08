package com.cn.pedido;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.text.*;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.http.client.methods.*;
import org.apache.http.message.*;
import org.apache.http.client.entity.*;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.AdapterView.*;
import android.content.Intent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.*;


public class SendOrder extends Activity {
	
	ArrayList<Order> listOrders= new ArrayList<Order>();
	ArrayList<Product> listProducts= new ArrayList<Product>();
	ListView lsvSammary;
	TextView lblSammaryCostTotal;
	AlertDialog.Builder dlConfirmacion;
	int indice=-1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_to_order);		
		
		lsvSammary=(ListView)findViewById(R.id.lsvSummary);		
		lblSammaryCostTotal=(TextView)findViewById(R.id.lblSummaryCostTotal);		
		
		Bundle extraOrders=getIntent().getBundleExtra("orders");
		listOrders=(ArrayList<Order>)extraOrders.getSerializable("ordersList");
		DecimalFormat df= new DecimalFormat("#");
		
		final ArrayList<String> from= new ArrayList<String>();
		for(int i=0;i<listOrders.size();i++)
			from.add(df.format(listOrders.get(i).getQuantity())+":"+listOrders.get(i).getName());		
		
		final StableArrayAdapter adapter= new StableArrayAdapter(this, android.R.layout.simple_list_item_1, from);
		lsvSammary.setAdapter(adapter);
		df= new DecimalFormat("#.##");
		lblSammaryCostTotal.setText("Costo Total: s/."+df.format(getIntent().getExtras().getDouble("totalCost")));	
		
		dlConfirmacion = new AlertDialog.Builder(this);
		dlConfirmacion.setTitle(".:: Aviso");
        dlConfirmacion.setMessage("¿ Estas seguro de quitar el producto?");
        dlConfirmacion.setCancelable(false);

        AlertDialog.Builder aceptar = dlConfirmacion.setPositiveButton("Aceptar", 
        new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialogo, int id) {
        		if(indice>-1){
					
        			listOrders.remove(indice);
        			indice=-1;
					DecimalFormat df= new DecimalFormat("#");
					final ArrayList<String> from= new ArrayList<String>();
					Double tempTotalCost=0.0;
					for(int i=0;i<listOrders.size();i++){						
						from.add(df.format(listOrders.get(i).getQuantity())+":"+listOrders.get(i).getName());
						tempTotalCost=tempTotalCost+listOrders.get(i).getQuantity()*listOrders.get(i).getPrice();
						}
					df= new DecimalFormat("#.##");
					lblSammaryCostTotal.setText("Costo Total: s/."+ df.format(tempTotalCost));
					
					final StableArrayAdapter adapter= new StableArrayAdapter(SendOrder.this, android.R.layout.simple_list_item_1, from);
					lsvSammary.setAdapter(adapter);
        		}
            }
        });
        
        dlConfirmacion.setNegativeButton("Cancelar", 
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo, int id) {
                
            }
        });
		

		lsvSammary.setOnItemClickListener(new  AdapterView.OnItemClickListener(){
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					indice=position;
					dlConfirmacion.show();
				} 
			});
	}	
	
	public void onFinishOrder(View v){
		try{
			StrictMode.ThreadPolicy policy= new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			
			HttpClient oHttpclient= new DefaultHttpClient();
			HttpPost oHttpPost = new HttpPost("http://loswaykis.com/ws/wspedidoinsert.php");
			List<BasicNameValuePair> namevaluePairs= new ArrayList<BasicNameValuePair>();
			
			for(int i=0; i<listOrders.size(); i++){
				
				namevaluePairs.add(new BasicNameValuePair("idOrders["+i+"]", listOrders.get(i).getIdOrder()));
				namevaluePairs.add(new BasicNameValuePair("idProducts["+i+"]", listOrders.get(i).getIdProduct()));
				namevaluePairs.add(new BasicNameValuePair("Quantities["+i+"]", ""+listOrders.get(i).getQuantity()));
			}
			
			oHttpPost.setEntity(new UrlEncodedFormEntity(namevaluePairs));
			HttpResponse response=oHttpclient.execute(oHttpPost);
			if(response.getStatusLine().getStatusCode()== HttpStatus.SC_OK){
				
					HttpEntity entity = response.getEntity();
					BufferedHttpEntity buffer = new BufferedHttpEntity(entity);
					InputStream iStream = buffer.getContent();


					String aux = "";

					BufferedReader r = new BufferedReader(new InputStreamReader(iStream));
					String line;
					while ((line = r.readLine()) != null) {
						aux += line;
					}

					JSONObject jsonObject = new JSONObject(aux);
					JSONArray result = jsonObject.getJSONArray("result");

				if(result.getJSONObject(0).getString("status").equalsIgnoreCase("ok")){

					Toast.makeText(getApplicationContext(), "Su pedido fue registrado satisfactoriamente, en unos instantes nos contactaremos con usteed.", 50000).show();
					listOrders.clear();
					Bundle orders= new Bundle();
					orders.putSerializable("ordersList", listOrders);
					Intent intent = getIntent();
					intent.putExtra("updateOrders", orders);
					setResult(2, intent);
					finish();
				}					
			}

			
		}
		catch(Exception ex){
			Toast.makeText(getApplicationContext(), ""+ex.getMessage(),Toast.LENGTH_SHORT).show();
			
		}
	
	}

	 private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
            List<String> objects) {
          super(context, textViewResourceId, objects);
          for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
          }
        }

        @Override
        public long getItemId(int position) {
          String item = getItem(position);
          return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
          return true;
        }

    }

	@Override
	public void onBackPressed()
	{
		// TODO: Implement this method
		Bundle orders= new Bundle();
		orders.putSerializable("ordersList", listOrders);
		Intent intent = getIntent();
		intent.putExtra("updateOrders", orders);
		setResult(2, intent);
		super.onBackPressed();
	}

	
}
