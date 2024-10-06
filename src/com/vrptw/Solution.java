/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vrptw;

import ilog.concert.IloException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 *
 * @author Admin
 * Chức năng lớp: đánh giá tính khả thi của giải pháp
 */
public class Solution {
    private static DecimalFormat df = new DecimalFormat("0.00");
    double epsilon = 0.0001;
    Data data = new Data();
    ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
    ArrayList<ArrayList<Double>> servetimes = new ArrayList<>();
    public Solution(Data data, ArrayList<ArrayList<Integer>> routes, ArrayList<ArrayList<Double>> servetimes) {
        super();
        this.data = data;
        this.routes = routes;
        this.servetimes = servetimes;
    }
    //Chức năng hàm: so sánh kích thước của hai số
    public int doubleCompare(double v1,double v2) {
        if (v1 < v2 - epsilon) {
            return -1;
        }
        if (v1 > v2 + epsilon) {
            return 1;
        }
        return 0;
    }   
     
    // đánh giá tính khả thi của giải pháp
    public void fesible() throws IloException {
        //Feasibility judgment of the number of vehicles Đánh giá khả thi về số lượng phương tiện
        if (routes.size() > data.vecnum) {
            System.out.println("error: vecnum!!!");
            System.exit(0);
        }
        //Feasibility judgment of vehicle load Đánh giá khả thi về tải trọng xe
        for (int k = 0; k < routes.size(); k++) {
            ArrayList<Integer> route = routes.get(k);
            double capasity = 0;
            for (int i = 0; i < route.size(); i++) {
                capasity += data.demands[route.get(i)];
            }
            if (capasity > data.cap) {
                System.out.println("error: cap!!!");
                System.exit(0);
            }
        }
        //Feasibility judgment of time window and vehicle capacity Đánh giá khả thi về khoảng thời gian và sức chứa của xe
        System.out.println("-----------------------------------------------------------------------------");
        double totalTimeOfTheRoute =0;
        for (int k = 0; k < routes.size(); k++) {
            System.out.println("Route for vehicle " + (k+1) + ":");
            ArrayList<Integer> route = routes.get(k);
            ArrayList<Double> servertime = servetimes.get(k);
            double capasity = 0;
            double timeOfTheRoute =0;
            String trackRoute = ""; 
            String trackTW = "";
            String trackCST = "";
            String trackDest="";
            for (int i = 0; i < route.size()-1; i++) {
                int origin = route.get(i);
                int destination = route.get(i+1);
                double si = servertime.get(i); // Thời gian phục vụ tại khách hàng i
                double sj = servertime.get(i+1); // Thời gian phục vụ tại khách hàng j
                String tmpRoute = ""; 
                
                
                // [𝑎𝑖,𝑏𝑖]: Time window for customer 𝑖 (𝑖∈𝑁).
                if (si < data.a[origin] && si >  data.b[origin]) {
                    System.out.println("error: servertime!");
                    System.exit(0);
                }
                if (doubleCompare(si + data.dist[origin][destination],data.b[destination]) > 0) {
                    System.out.println(origin + ": [" + data.a[origin] + ","+data.b[origin]+"]"+ " "+ si);
                    System.out.println(destination + ": [" + data.a[destination] + ","+data.b[destination]+"]"+ " "+ sj);
                    System.out.println(data.dist[origin][destination]);
                    System.out.println(destination + ":" );
                    System.out.println("error: forward servertime!"); // lỗi: chuyển tiếp thời gian máy chủ
                    System.exit(0);
                }
                if (doubleCompare(sj - data.dist[origin][destination],data.a[origin]) < 0) {
                    System.out.println(origin + ": [" + data.a[origin] + ","+data.b[origin]+"]"+ " "+ si);
                    System.out.println(destination + ": [" + data.a[destination] + ","+data.b[destination]+"]"+ " "+ sj);
                    System.out.println(data.dist[origin][destination]);
                    System.out.println(destination + ":" );
                    System.out.println("error: backward servertime!");
                    System.exit(0);
                }
                
                tmpRoute += "    Origin: "+origin + "; TW[" + data.a[origin] + ","+data.b[origin]+"]; CST: "+ df.format(si)+
                "; Destination: "+destination + "; TW[" + data.a[destination] + ","+data.b[destination]+"]; CST: "+ df.format(sj);
                // khong can in doan nay nua
                //System.out.println(tmpRoute);
                timeOfTheRoute += sj;
                
                trackRoute += origin + " {TW("+ data.a[origin] + ","+data.b[origin]+"), CST: "+df.format(si)+"} -> ";
                if (i== route.size()-2) {                    
                    trackTW = data.a[destination] + ","+data.b[destination];
                    trackCST = df.format(sj)+"";
                    trackDest = destination+"";
                }
            }
            totalTimeOfTheRoute += timeOfTheRoute;
            if(!trackDest.equals("")) {
                trackRoute += trackDest + " {TW("+trackTW+"), CST: "+trackCST +"}";
            }
            //System.out.println("    [ "+trackRoute+" ]");
            System.out.println("    "+trackRoute+"");
            System.out.println("    Time of the route: "+df.format(timeOfTheRoute) +" min");
            if (capasity > data.cap) {
                System.out.println("error: cap!!!");
                System.exit(0);
            }
        }
        System.out.println("Total time of all routes: "+df.format(totalTimeOfTheRoute) +" min");
        System.out.println("-----------------------------------------------------------------------------");
    } 
    // in ket qua ra:
    public void printSolution() {
        ArrayList<Integer> route = new ArrayList<>();
        ArrayList<Double> servertime = new ArrayList<>();
        try {
            for (int k = 0; k < routes.size(); k++) { // lap qua so luong xe
                System.out.println("Route for Vehicle " + k + ":");
                route = routes.get(k);
                servertime = servetimes.get(k);                   
                for (int i = 0; i < routes.size()-1; i++) {
                    /*
                    IntVar timeVar = timeDimension.cumulVar(index);
                    route += manager.indexToNode(index) + " Time(" + solution.min(timeVar) + ","
                        + solution.max(timeVar) + ") -> ";
                    index = solution.value(routing.nextVar(index));
                    */
                    
                    int origin = route.get(i);
                    int destination = route.get(i+1);
                    double si = servertime.get(i); // Thời gian phục vụ tại khách hàng i
                    double sj = servertime.get(i+1); // Thời gian phục vụ tại khách hàng j
                    System.out.println(origin + ": [" + data.a[origin] + ","+data.b[origin]+"]"+ " "+ si);
                    System.out.println(destination + ": [" + data.a[destination] + ","+data.b[destination]+"]"+ " "+ sj);
                    System.out.println(data.dist[origin][destination]);
                }
               
            }
        } catch (Exception ex) {
            System.out.println("Solution::printSolution() Exception !"+ex.getStackTrace());
        }
        
    }
}
