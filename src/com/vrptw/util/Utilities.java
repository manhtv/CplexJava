/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vrptw.util;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

/**
 *
 * @author Admin
 */
public class Utilities {
    
    /*------------------------------------------------------------------------------------------*/
    static void usageSearch() {      
        System.out.println("          A       AutoSearch  (0)");
        System.out.println("          T       Traditional (1)");       
        System.out.println("          D       Dynamic     (2)");      
    }  
    /*------------------------------------------------------------------------------------------*/
    public static void configureCplex(IloCplex cplex, String optMethodSearch) throws IloException {
        // Đánh giá tùy chọn dòng lệnh và thiết lập phương pháp tối ưu hóa tương ứng.
        System.out.println("Strategy Search :            "+optMethodSearch);
        System.out.println("Optimization mode:           "+cplex.getParam(IloCplex.Param.Parallel)); //default
        System.out.println("is MIP:                      "+cplex.isMIP());
        System.out.println("Algorithm Available at Root: "+cplex.getParam(IloCplex.Param.RootAlgorithm)); 
        System.out.println("Algorithm Types for NodeAlg: "+cplex.getParam(IloCplex.Param.NodeAlgorithm));         
        switch (optMethodSearch.toUpperCase()) { // Liệt kê các giá trị có thể cho tham số tìm kiếm MIP. IloCplex.IntParam.MIPSearch
            case "A": cplex.setParam(IloCplex.Param.MIP.Strategy.Search, IloCplex.MIPSearch.Auto); 
                // Automatic: let CPLEX choose; default
                System.out.println("The MIPsearch is AutoSearch (0)");
                break;
            case "T": cplex.setParam(IloCplex.Param.MIP.Strategy.Search, IloCplex.MIPSearch.Traditional); 
                // Apply traditional branch and cut strategy; disable dynamic search
                System.out.println("The MIPsearch is Traditional (1)");
                break;
            case "D": cplex.setParam(IloCplex.Param.MIP.Strategy.Search, IloCplex.MIPSearch.Dynamic); 
                // Apply dynamic search
                System.out.println("The MIPsearch is Dynamic (2)");
                break;
            default:  usageSearch();
               return;
        }
          // CHIEN LUOC SEARCH
//        ilog.cplex.IloCplex.MIPSearch
//        public static final int	Auto	0
//        public static final int	Dynamic	2
//        public static final int	Traditional	1

        
          // CHAY MODE
//        ilog.cplex.IloCplex.ParallelMode
//        public static final int	Auto	0
//        public static final int	Deterministic	1
//        public static final int	Opportunistic	-1

       // Algorithm types for RootAlg (for LP)
//        0: IloCplex.Algorithm.Auto
//        1: IloCplex.Algorithm.Primal
//        3 IloCplex.Algorithm.Network
//        4 IloCplex.Algorithm.Barrier
//        5 IloCplex.Algorithm.Sifting
//        6 IloCplex.Algorithm.Concurrent

        // Algorithm types for NodeAlg (for MILP)
//        0 IloCplex.Algorithm.Auto
//        1 IloCplex.Algorithm.Primal
//        2 IloCplex.Algorithm.Dual
//        3 IloCplex.Algorithm.Network
//        4 IloCplex.Algorithm.Barrier
//        5 IloCplex.Algorithm.Sifting
    }  
}
