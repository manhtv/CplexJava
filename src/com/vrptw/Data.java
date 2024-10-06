/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vrptw;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
/*
  * https://www.sintef.no/projectweb/top/vrptw/solomon-benchmark/documentation/ Format data set
  *
  */
// Define parameters (Xác định các thông số)https://www.sintef.no/projectweb/top/vrptw/solomon-benchmark/documentation/
public class Data {
    // dung de format so in ra ngoai
    public static DecimalFormat df = new DecimalFormat("0.00");
    // All points set n (including distribution center and customer points, the beginning and the end (0 and n) 
    // are the distribution center)
    int vetexnum;				//Tất cả các điểm đặt n (bao gồm trung tâm phân phối và điểm khách hàng, điểm đầu tiên và điểm cuối cùng (0 và n) là trung tâm phân phối)
                                                // ( Lưu số dòng trong file dataset thôi, ví dụ C101.txt thì lưu CUST NO từ 0 đến 100 ( giá trị 
                                                // N trong tài liệu nhưng có cả phẩn tử 0 (departing depot) và n+1 (returning depot))    
    // Earliest departure time (2015_Designing an On-Line Ride-Sharing System (duoc ap dung cho bai home2work) trong F:\MEGAsync\NC\Plan\Paper1\Refer\Architecture)
    double E;	      				//Start time of distribution center time window Thời gian bắt đầu của cửa sổ thời gian trung tâm phân phối (thấy gán a0 = E (ea?))
    // Latest arrival time: t(w)
    double L;	     			 	// End time of distribution center time window Thời gian kết thúc của cửa sổ thời gian trung tâm phân phối (thấy gán bn+1 = L (latest))
    int vecnum;    				//Number of vehicles Số lượng xe
    double cap;     				//Vehicle load Trọng tải xe (capacity) ( ngưỡng đó)
    int[][] vertexs;				//The coordinates x,y of all points Tọa độ x, y của tất cả các điểm
    int[] demands;                              //Demand Nhu cầu (on demand)
    int[] vehicles;				//Vehicle number Số xe
    double[] a;						//Start time of time window [a[i],b[i]] Thời gian bắt đầu của khoảng thời gian [a [i], b [i]]
    double[] b;						//End time of time window [a[i],b[i]] Khoảng thời gian kết thúc của khoảng thời gian [a [i], b [i]]
    double[] s;						//Customer service time Thời gian phục vụ khách hàng
    int[][] arcs;					//arcs[i][j] represents the arc from i to j arcs [i] [j] đại diện cho cung từ i đến j    
    double[][] dist;				//Ma trận khoảng cách thỏa mãn mối quan hệ tam giác, và tạm thời sử dụng khoảng cách để biểu diễn chi phí C [i] [j] = dist [i] [j]
    //Truncated decimal 3.264434->3.2 Số thập phân bị cắt ngắn 3,264434-> 3,2
    public double doubleTruncate(double v){
        double precisionThreshold = 0.000000000001;
        int iv = (int) v; // lấy phần nguyên
        if(iv+1 - v <= precisionThreshold) {
            return iv+1;
        }
        double dv = (v - iv) * 10;
        int idv = (int) dv;
        double rv = iv + idv / 10.0;
        return rv;
    }
}

