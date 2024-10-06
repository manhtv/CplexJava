/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vrptw;

import com.vrptw.util.Utilities;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Admin
 */
//Chức năng lớp: xây dựng mô hình và giải quyết
public class Vrptw {
    
    // dung de format so in ra ngoai
    private static DecimalFormat df = new DecimalFormat("0.00");
    
    Data data;		// Xác định các đối tượng của dữ liệu lớp
    IloCplex model;	// Định nghĩa các đối tượng của lớp bên trong cplex
    // x và w là hai kiểu biến trong mô hình : flow variables (X) và time variables (W)
    // Wik: specifying the start of service at node i when serviced by vehicle k
    // w liên quan đến tính toán servetimes cho đầu ra mô hình
    public IloNumVar[][][] x;	// x[i][j][k] nghĩa là cung [i] [j] được xe k ghé thăm
    public IloNumVar[][] w;	// Ma trận thời gian của xe đến tất cả các điểm
    double cost;		// Target value (Giá trị của mục tiêu)
    Solution solution;			
    /*-------------------------------------------------------------------------------------------------------*/
    public Vrptw(Data data) { // hàm khởi tạo
        this.data = data;
    }
    /*-------------------------------------------------------------------------------------------------------*/
    //Chức năng chức năng: giải mô hình, tạo đường dẫn xe và nhận giá trị mục tiêu    
    public void solve(String optMethod) throws IloException {
        // Định nghĩa danh sách liên kết đường đi của phương tiện.
        ArrayList<ArrayList<Integer>> routes = new ArrayList<>();
        // Định nghĩa danh sách thời gian phục vụ.
        ArrayList<ArrayList<Double>> servetimes = new ArrayList<>();
        //Khởi tạo danh sách liên kết của lộ trình phương tiện và thời gian đã sử dụng, độ dài của danh sách liên kết này là số lượng phương tiện k.
        for (int k = 0; k < data.vecnum; k++) {
            ArrayList<Integer> r = new ArrayList<>();	// Xác định một danh sách liên kết kiểu int (r: route)
            ArrayList<Double> t = new ArrayList<>();	// Xác định đối tượng dưới dạng danh sách liên kết kiểu double (t: time)
            //Thêm danh sách liên kết đã định nghĩa ở trên vào các lộ trình danh sách liên kết.
            routes.add(r);				// Thêm danh sách liên kết được xác định ở trên vào các tuyến danh sách liên kết
            servetimes.add(t);			// Same as above Giống như trên
        }  
        // Gọi hàm cấu hình mô hình / thuật toán để giải
        Utilities.configureCplex(model, optMethod);        
        if(model.solve() == false){ // giải bài toán MIP, sau đó cố định các biến nguyên
        //if(model.solveFixed() == false){ // giải lại bài toán với các biến nguyên đã cố định            
            System.out.println("Problem should not solve false!!!");
            return;	//Nếu không thể giải quyết được, hãy trực tiếp thoát ra khỏi hàm solve
        } else {
            //Lặp qua mỗi phương tiện
            for(int k = 0; k < data.vecnum; k++){
                boolean terminate = true;
                int i = 0;
                routes.get(k).add(0);		
                servetimes.get(k).add(0.0);
                while(terminate){
                    for (int j = 0; j < data.vetexnum; j++) {
                        if (data.arcs[i][j]>=0.5 && model.getValue(x[i][j][k])>=0.5) {
                            routes.get(k).add(j);
                            servetimes.get(k).add(model.getValue(w[j][k]));
                            i = j;
                            break;
                        }
                    } if (i == data.vetexnum-1) {
                        terminate = false;
                    }
                }
            }
        }
        // Lay nhung thong tin nay sau hien thi
        solution = new Solution(data,routes,servetimes);
        cost = model.getObjValue();
//        System.out.println("routes="+solution.routes);
//        System.out.println("Returns the algorithm that was used to generate the current solution: "+model.getAlgorithm());
//        
//System.out.println("servetimes="+solution.servetimes);
//        solution.printSolution();
        
        //System.out.println("servetimes="+solution.servetimes);
    }
    /*-------------------------------------------------------------------------------------------------------*/
    //Chức năng: Xây dựng mô hình CPLEX cho bài toán VRPTW dựa trên mô hình toán học của VRPTW    
    private void buildModel() throws IloException {
        //model
        model = new IloCplex();
        model.setOut(null); //cấu hình để không in ra phần log track
        //variables
        x = new IloNumVar[data.vetexnum][data.vetexnum][data.vecnum];
        w = new IloNumVar[data.vetexnum][data.vecnum];	//Time of vehicle access point Thời gian của điểm truy cập xe
        //Xác định kiểu dữ liệu và phạm vi gitrị của các biến song công x và w
        // Define the data type and value range of cplex variables x and w
        for (int i = 0; i < data.vetexnum; i++) {
            for (int k = 0; k < data.vecnum; k++) {
                // 𝑤𝑖𝑘: the time when vehicle 𝑘 starts to serve customer 𝑖
                /*
                IloNumVar numVar(double lb, double ub, IloNumVarType type, java.lang.String name):
                    lb - The lower bound of the new numeric variable.
                    ub - The upper bound of the new numeric variable.
                    type - The type of the new numeric variable.
                    name - The name of the new numeric variable.                
                Creates and returns a new modeling object, a numeric variable with bounds, type, and name.
                This method returns an object representing a new modeling variable with the specified bounds, type, and name.
                1e15 = 1,000,000,000,000,000.00
                
                */
                w[i][k] = model.numVar(0, 1e15, IloNumVarType.Float, "w" + i + "," + k);
            }
            for (int j = 0; j < data.vetexnum; j++) {
                if (data.arcs[i][j]==0) { // nếu không có cung đi từ i đến j
                    x[i][j] = null;
                }
                else{
                    //Xijk, formulas (10)-(11) Xijk, công thức (10) - (11).
                    // (10) Xijk > 0 với mọi k thuộc K, (i,j) thuộc A 
                    // (11) Xijk thuộc {0,1} với mọi k thuộc K, (i,j) thuộc A 
                    for (int k = 0; k < data.vecnum; k++) {
                        x[i][j][k] = model.numVar(0, 1, IloNumVarType.Int, "x" + i + "," + j + "," + k);
                    }
                }
            }
        }
        //Add objective function (Thêm chức năng mục tiêu)
        //Formula (1) (công thức 1) minCij, Xijk đó
        IloNumExpr obj = model.numExpr();
        for(int i = 0; i < data.vetexnum; i++){
            for(int j = 0; j < data.vetexnum; j++){
                if (data.arcs[i][j]==0) { // nếu không có cung đi từ i đến j
                    continue;
                }
                for(int k = 0; k < data.vecnum; k++){
                    /*
                    1. IloNumExpr sum(IloNumExpr e1, IloNumExpr e2) throws IloException.
                    Params:
                        e1 - The first numeric expression.
                        e2 - The second numeric expression.
                    Return:
                        A numeric expression representing the sum of e1 + e2.
                    Adds two numeric expressions and returns the sum.
                    The domain of the resulting expression is computed from the domains of the combined expressions.
                    2. IloNumExpr prod(IloNumExpr e, double v)throws IloException.
                    Creates and returns an expression representing the product of the expression e and the value v.
                    Params:
                        e - An expression to use in the product.
                        v - A value to add.
                    Return:
                        An expression representing the product e * v.
                    */
                    obj = model.sum(obj, model.prod(data.dist[i][j], x[i][j][k]));
                }
            }
        }
        /*
        3. IloObjective addMinimize(IloNumExpr expr) throws IloException
        Creates and returns an instance of IloObjective representing an objective to minimize the 
        expression expr and added to the invoking model.
        Params:
            expr - Expression to minimize.                        
        Return:
            An IloObjective object representing the objective to minimize expr.
        */
        model.addMinimize(obj);
        //Join constraints (Tham gia các ràng buộc)
        //Formula(2) (công thức 2): Hạn chế việc chỉ định mỗi KH cho đúng một tuyến phương tiện
        for(int i= 1; i < data.vetexnum-1;i++){
            IloNumExpr expr1 = model.numExpr();
            for (int k = 0; k < data.vecnum; k++) {
                for (int j = 1; j < data.vetexnum; j++) {
                    if (data.arcs[i][j]==1) {
                        expr1 = model.sum(expr1, x[i][j][k]);
                    }
                }
            }
            model.addEq(expr1, 1);
        }
        // TỪ (3) ĐẾN (5): đặc trưng cho luồng trên đường đi mà phương tiên k chạy
        //Formula(3) Công thức (3) Tong X0jk =1 : 
        for (int k = 0; k < data.vecnum; k++) {
            IloNumExpr expr2 = model.numExpr();
            for (int j = 1; j < data.vetexnum; j++) {
                if (data.arcs[0][j]==1) {
                    expr2 = model.sum(expr2, x[0][j][k]);
                }
            }
            model.addEq(expr2, 1);
        }
        //Formula(4) 2 tong - cho nhau =0 
        for (int k = 0; k < data.vecnum; k++) {
            for (int j = 1; j < data.vetexnum-1; j++) {
                IloNumExpr expr3 = model.numExpr();
                IloNumExpr subExpr1 = model.numExpr();
                IloNumExpr subExpr2 = model.numExpr();
                for (int i = 0; i < data.vetexnum; i++) {
                    if (data.arcs[i][j]==1) {
                        subExpr1 = model.sum(subExpr1,x[i][j][k]);
                    }
                    if (data.arcs[j][i]==1) {
                        subExpr2 = model.sum(subExpr2,x[j][i][k]);
                    }
                }
                expr3 = model.sum(subExpr1,model.prod(-1, subExpr2));
                model.addEq(expr3, 0);
            }
        }
        //Formula(5): 
        for (int k = 0; k < data.vecnum; k++) {
            IloNumExpr expr4 = model.numExpr();
            for (int i = 0; i < data.vetexnum-1; i++) {
                if (data.arcs[i][data.vetexnum-1]==1) {
                    expr4 = model.sum(expr4,x[i][data.vetexnum-1][k]);
                }
            }
            model.addEq(expr4, 1);
        }
        // TƯ (6) - (8) VÀ (9): đảm bảo tính khả thi của lịch trình tương ứng đối với các cân 
        // nhắc về thời gian và khía cạnh công suất phương tiện.
        // Lưu ý rằng với k cho trước, các ràng buộc (2.7) buộc w_ik=0 bất cứ khi nào khách hàng i không có phương tiện k đến thăm.
        //Formula(6)
        double M = 1e5;
        for (int k = 0; k < data.vecnum; k++) {
            for (int i = 0; i < data.vetexnum; i++) {
                for (int j = 0; j < data.vetexnum; j++) {
                    if (data.arcs[i][j] == 1) {
                        IloNumExpr expr5 = model.numExpr();
                        IloNumExpr expr6 = model.numExpr();
                        expr5 = model.sum(w[i][k], data.s[i]+data.dist[i][j]);
                        expr5 = model.sum(expr5,model.prod(-1, w[j][k]));
                        expr6 = model.prod(M,model.sum(1,model.prod(-1, x[i][j][k])));
                        model.addLe(expr5, expr6);
                    }
                }
            }
        }
        //Formula(7)
        for (int k = 0; k < data.vecnum; k++) {
            for (int i = 1; i < data.vetexnum-1; i++) {
                IloNumExpr expr7 = model.numExpr();
                for (int j = 0; j < data.vetexnum; j++) {
                    if (data.arcs[i][j] == 1) {
                        expr7 = model.sum(expr7,x[i][j][k]);
                    }
                }
                model.addLe(model.prod(data.a[i], expr7), w[i][k]);
                model.addLe(w[i][k], model.prod(data.b[i], expr7));
            }
        }
        //Formula(8)
        for (int k = 0; k < data.vecnum; k++) {
            model.addLe(data.E, w[0][k]);
            model.addLe(data.E, w[data.vetexnum-1][k]);
            model.addLe(w[0][k], data.L);
            model.addLe(w[data.vetexnum-1][k], data.L);
        }
        //Formula(9)
        for (int k = 0; k < data.vecnum; k++) {
            IloNumExpr expr8 = model.numExpr();
            for (int i = 1; i < data.vetexnum-1; i++) {
                IloNumExpr expr9 = model.numExpr();
                for (int j = 0; j < data.vetexnum; j++) {
                    if (data.arcs[i][j] == 1) {
                        expr9=model.sum(expr9,x[i][j][k]);
                    }
                }
                expr8 = model.sum(expr8,model.prod(data.demands[i],expr9));
            }
            model.addLe(expr8, data.cap);
        }
    }
    /*-------------------------------------------------------------------------------------------------------*/
    //Chức năng chức năng: đọc dữ liệu từ tệp txt và khởi tạo các tham số
    public static void processSolomon(String path,Data data,int vetexnum) throws Exception{
        String line = null;
        String[] substr = null;
        Scanner cin = new Scanner(new BufferedReader(new FileReader(path)));  //Read file Đọc tài liệu
        for(int i =0; i < 4;i++){
            line = cin.nextLine();  //Read a line
        }
        line = cin.nextLine();
        line.trim();
        substr = line.split(("\\s+")); //Tách chuỗi bằng dấu cách làm dấu
        //Initialization parameters Tham số khởi tạo
        data.vetexnum = vetexnum; // số lượng node 
        data.vecnum = Integer.parseInt(substr[1]);  // NUMBER K trong solomon document
        data.cap = Integer.parseInt(substr[2]);     // CAPACITY Q
        data.vertexs =new int[data.vetexnum][2];    // The coordinates x,y of all points Tọa độ x, y của tất cả các điểm (XCOORD, YCOORD)
        data.demands = new int[data.vetexnum];      // Demand Nhu cầu
        data.vehicles = new int[data.vecnum];       // vehicle number Số xe
        data.a = new double[data.vetexnum];         // start time of the time window Thời gian bắt đầu cửa sổ thời gian (READY TIME)
        data.b = new double[data.vetexnum];         // end time of time window Thời gian kết thúc cửa sổ thời gian (DUE TIME)
        data.s = new double[data.vetexnum];         // service time Giờ phục vụ (SERVICE TIME)
        data.arcs = new int[data.vetexnum][data.vetexnum];
        // Ma trận khoảng cách, đảm bảo mối quan hệ tam giác (bất đẳng thức tam giác)
        data.dist = new double[data.vetexnum][data.vetexnum];	//Ma trận khoảng cách thỏa mãn mối quan hệ tam giác và chi phí (cost) được biểu thị bằng khoảng cách
        
        for(int i =0; i < 4;i++){ // phần header của file 
            line = cin.nextLine();
            //System.out.println("line["+i+"]: "+line);
        }
        //System.out.println("data.vetexnum==>"+data.vetexnum);
        //Đọc dữ liệu hàng vetexnum-1
        for (int i = 0; i < data.vetexnum - 1; i++) {
            line = cin.nextLine();            
            line.trim();
            //System.out.println("line["+i+"]: "+line);
            substr = line.split("\\s+"); 
            for (int j = 0; j < substr.length; j++) {
                //System.out.println("substr["+j+"]: "+substr[j]); // only for debugging
            }
            data.vertexs[i][0] = Integer.parseInt(substr[2]);           //lưu tọa tộ đỉnh x (XCOORD)
            data.vertexs[i][1] = Integer.parseInt(substr[3]);           //lưu tọa tộ đỉnh y (YCOORD)
            data.demands[i] = Integer.parseInt(substr[4]);              //lưu demand (DEMAND)
            data.a[i] = Integer.parseInt(substr[5]);                    // READY TIME (Đơn vị là phút hợp lý)
            data.b[i] = Integer.parseInt(substr[6]);                    // DUE DATE   (Đơn vị là phút hợp lý)
            data.s[i] = Integer.parseInt(substr[7]);                    // SERVICE TIME (Đơn vị là phút hợp lý)
        }
        cin.close();//Close stream đóng luồng
        //Khởi tạo các thông số trung tâm phân phối
        data.vertexs[data.vetexnum-1] = data.vertexs[0];                // khoi tao đỉnh n giá trị tại 0
        data.demands[data.vetexnum-1] = 0;
        data.a[data.vetexnum-1] = data.a[0];
        data.b[data.vetexnum-1] = data.b[0];
        data.E = data.a[0];
        data.L = data.b[0];
        data.s[data.vetexnum-1] = 0;		
        double min1 = 1e15;
        double min2 = 1e15;
        //Khởi tạo ma trận khoảng cách
        for (int i = 0; i < data.vetexnum; i++) {
            for (int j = 0; j < data.vetexnum; j++) {
                if (i == j) {
                    data.dist[i][j] = 0;
                    continue;
                }
                // build ma tran khoang cach (euclid)
                data.dist[i][j] = Math.sqrt((data.vertexs[i][0]-data.vertexs[j][0])*(data.vertexs[i][0]-data.vertexs[j][0])+
                                  (data.vertexs[i][1]-data.vertexs[j][1])*(data.vertexs[i][1]-data.vertexs[j][1]));
                data.dist[i][j]=data.doubleTruncate(data.dist[i][j]);
            }
        }
//        System.out.println("Print ma tran khoang cach: ");
//        for (int i = 0; i <  data.dist.length; i++) { 
//            for (int j = 0; j < data.dist.length; j++) { 
//                //System.out.print("dist["+i+"]["+j+"]: "+data.dist[i][j] + " "); 
//                System.out.print(data.dist[i][j] + "    "); 
//            }
//            System.out.println();
//        }
        data.dist[0][data.vetexnum-1] = 0;
        data.dist[data.vetexnum-1][0] = 0;
        //Ma trận khoảng cách thỏa mãn mối quan hệ tam giác (The distance matrix satisfies the triangular relationship)
        // Cần phải kiểm tra lai đoạn này (https://www.researchgate.net/post/How_to_satisfy_triangular_inequality_in_asymmetric_distance_vehicle_routing_problem)
        // những thằng nào không thỏa mãn đk bất đẳng thức tam giac thì set lại gia trị thôi
        for (int  k = 0; k < data.vetexnum; k++) {
            for (int i = 0; i < data.vetexnum; i++) {
                for (int j = 0; j < data.vetexnum; j++) {
                    if (data.dist[i][j] > data.dist[i][k] + data.dist[k][j]) {
                        data.dist[i][j] = data.dist[i][k] + data.dist[k][j];
                    }
                }
            }
        }
//        System.out.println("Print ma tran khoang cach: ");
//        for (int i = 0; i <  data.dist.length; i++) { 
//            for (int j = 0; j < data.dist.length; j++) { 
//                //System.out.print("d["+i+"]["+j+"]: "+data.dist[i][j] + "    "); 
//                System.out.print(data.dist[i][j] + "    ");
//            }
//            System.out.println();
//        }
        //Khởi tạo để hoàn thành biểu đồ ( khởi tạo các cung)
        // Initialize to complete graph
        for (int i = 0; i < data.vetexnum; i++) {
            for (int j = 0; j < data.vetexnum; j++) {
                if (i != j) {
                    data.arcs[i][j] = 1;
                }
                else {
                    data.arcs[i][j] = 0;
                }
            }
        }
        //Loại bỏ các cạnh không đáp ứng các hạn chế về thời gian và dung lượng        
        for (int i = 0; i < data.vetexnum; i++) {
            for (int j = 0; j < data.vetexnum; j++) {
                if (i == j) {
                    continue;
                }
                
                // a[i] + s[i] + dist[i][j] > b[j] OR demands[i]+demands[j]>cap                
                if (data.a[i]+data.s[i]+data.dist[i][j]>data.b[j] || data.demands[i]+data.demands[j]>data.cap) {
                    data.arcs[i][j] = 0;
                }
                // a[0] + s[i] + dist[0][i] + dist[i][vetexnum-1] > b[vetexnum-1]
                if (data.a[0]+data.s[i]+data.dist[0][i]+data.dist[i][data.vetexnum-1]>data.b[data.vetexnum-1]) {
                    System.out.println("the calculating example is false");
                }
            }
        }
        for (int i = 1; i < data.vetexnum-1; i++) {
            if (data.b[i] - data.dist[0][i] < min1) {
                min1 = data.b[i] - data.dist[0][i];
            }
            if (data.a[i] + data.s[i] + data.dist[i][data.vetexnum-1] < min2) {
                min2 = data.a[i] + data.s[i] + data.dist[i][data.vetexnum-1];
            }
        }
        if (data.E > min1 || data.L < min2) {
            System.out.println("Duration false!");
            System.exit(0);//Terminate the program Chấm dứt chương trình
        }
        //Khởi tạo các tham số của trung tâm phân phối 0, n + 1
        // Initialize the parameters of the distribution center 0, n+1
        data.arcs[data.vetexnum-1][0] = 0;
        data.arcs[0][data.vetexnum-1] = 1;
        for (int i = 1; i < data.vetexnum-1; i++) {
            data.arcs[data.vetexnum-1][i] = 0;
        }
        for (int i = 1; i < data.vetexnum-1; i++) {
            data.arcs[i][0] = 0;
        }
    }
    /*-------------------------------------------------------------------------------------------------------*/
    public static void main(String[] args) {
        
         try {
            double cplex_time1 = System.nanoTime();
            Data data = new Data();
            /*
                Điều chỉnh tham số vertexnum (Số lượng đỉnh/điểm). Bao gồm cả đỉnh 0 là đỉnh bắt đầu, n+1 là đỉnh kết thúc.
                Vậy nếu bạn muốn chạy với 100 customer thì vertexnum = 102                
            */
            int vertexnum = -1;
            vertexnum = 35; // cho 33 customer
            
            // Đường dẫn đến file dữ liệu đầu vào
            String path = "";
            // dữ liệu khách hàng đã được phân cum với số lượng 33                    
            path = "C:\\Project\\CplexJava\\datasheet\\case_clustered\\c101_customer_33.txt"; 
            // dữ liệu khách hàng ngẫu nhiên với số lượng 33  
            path = "C:\\Project\\CplexJava\\datasheet\\case_randomly\\r101_customer_33.txt";
            System.out.println("path==>"+path);
            processSolomon(path,data,vertexnum);
            System.out.println("Cplex Procedure!");
            Vrptw cplex = new Vrptw(data);
            cplex.buildModel();
            // Bạn có thể chọn phương án tối ưu , ví dụ "A": AutoSearch, "D": Dynamic và "T": Traditional            
//            cplex.solve("A");
//            cplex.solve("D");
            cplex.solve("T");
            
            cplex.solution.fesible();
            double cplex_time2 = System.nanoTime();
            double cplex_time = (cplex_time2 - cplex_time1) / 1e9;//Thời gian giải quyết, đơn vị s (Solving time, unit s)
            System.out.println("Cplex time: " + df.format(cplex_time) + " seconds; best cost: " + df.format(cplex.cost) );            
            System.out.println("Status: "+cplex.model.getStatus().toString()); 
            System.out.println("routes="+cplex.solution.routes);
            System.out.println("Returns the algorithm that was used to generate the current solution: "+cplex.model.getAlgorithm());
            System.out.println("servetimes="+cplex.solution.servetimes);            
            System.out.println("chien luoc search la gi: "+cplex.model.getParam(IloCplex.Param.MIP.Strategy.Search));  
            cplex.model.end();// them ham release Releases the IloCplex object and the associated objects created by calls of the methods of the invoking object.
        } catch (Exception ex) {
             ex.printStackTrace();
        }
    }
}
