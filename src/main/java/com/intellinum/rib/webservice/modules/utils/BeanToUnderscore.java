package com.intellinum.rib.webservice.modules.utils;

import java.util.ArrayList;

/**
 * Created by Otniel on 5/15/2015.
 */
public class BeanToUnderscore {
    private String beanVariable;
    private String underScoredVariable;

    public BeanToUnderscore(){}

    public BeanToUnderscore(String beanVariable){
        this.beanVariable = beanVariable;
    }

    public void setBeanVariable(String beanVariable){
        this.beanVariable = beanVariable;
    }

    public String getUnderScoredVariable(){
        boolean hasUpperCase = !beanVariable.equals(beanVariable.toLowerCase());
        ArrayList<Integer> indexList = new ArrayList<Integer>();

        if(hasUpperCase){
            int upperCaseIndex = 0;

            while(upperCaseIndex < beanVariable.length()){
                if(Character.isUpperCase(beanVariable.charAt(upperCaseIndex))){
                    indexList.add(upperCaseIndex);
                }
                upperCaseIndex++;
            }

            underScoredVariable = "";

            for(int i=0; i<indexList.size(); i++){
                underScoredVariable += beanVariable.substring(0,indexList.get(i))+"_";
            }

            underScoredVariable+=beanVariable.substring(indexList.get(indexList.size()-1), beanVariable.length());
        }

        return  underScoredVariable.toLowerCase();
    }

    public String getUnderScoredVariable(String beanVariable){
        boolean hasUpperCase = !beanVariable.equals(beanVariable.toLowerCase());
        ArrayList<Integer> indexList = new ArrayList<Integer>();

        if(hasUpperCase){
            int upperCaseIndex = 0;

            while(upperCaseIndex < beanVariable.length()){
                if(Character.isUpperCase(beanVariable.charAt(upperCaseIndex))){
                    indexList.add(upperCaseIndex);
                }
                upperCaseIndex++;
            }

            underScoredVariable = "";

            for(int i=0; i<indexList.size(); i++){
                underScoredVariable += beanVariable.substring(0,indexList.get(i))+"_";
            }

            underScoredVariable+=beanVariable.substring(indexList.get(indexList.size()-1), beanVariable.length());
        }
        else{
            underScoredVariable = beanVariable;
        }

        return  underScoredVariable.toLowerCase();
    }
}
