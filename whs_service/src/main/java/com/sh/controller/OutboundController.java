package com.sh.controller;

import com.sh.model.dto.*;
import com.sh.model.dto.json.SelOutboundOrder;
import com.sh.model.service.OutboundService;
import com.sh.model.service.SupervisionService;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OutboundController {
    private OutboundService outboundService = new OutboundService();
    private SupervisionService supervisionService = new SupervisionService();

    public List<Boolean> outbLogic(List<SelOutboundOrder> selOutboundOrders){
        List<Boolean> outbLogicList = new ArrayList<>();
        boolean flag = true;
          try {
              outboundService.get_SqlSession();
           for (SelOutboundOrder selOutboundOrder : selOutboundOrders) {
               if (selOutboundOrder.getProductCount() > outboundService.checkItemCount(selOutboundOrder.getItemName())) {
                   outbLogicList.add(false);

                   System.out.println("재고없음");
                   flag = false;
               } else {
                   flag = true;
                   System.out.println("출고중");
                   outbLogicList.add(true);
                   long outbPk = outboundService.createOutbTB(selOutboundOrder.getCusNM());
                   System.out.println("출고서 상세 정보 생성");

                   long outbDetailPk = outboundService.createOutbDetailTB(outbPk, selOutboundOrder.getItemName(), selOutboundOrder.getProductCount());

                   long outbCartPk = outboundService.createOutbCartTB(outbDetailPk, selOutboundOrder.getProductCount());

                   System.out.println("카트 할당중");
                   outboundService.createOutBItemDetailTB(selOutboundOrder.getItemName(), selOutboundOrder.getProductCount(), selOutboundOrder.getProductCount());
                     System.out.println("출고 화장품 상세 생성");
                   long itemId = outboundService.searchItemId(selOutboundOrder.getItemName());

                   outboundService.updareItemCnt(selOutboundOrder.getProductCount() * -1, itemId);
                       System.out.println("재고 수량 감소");
                   for (int i = 0; i < selOutboundOrder.getProductCount(); i++) {
                       long itemDetailPk = outboundService.selectForDeleteItemDetail(selOutboundOrder.getItemName());

                       LocateDto lpn = outboundService.searchItemDetailLpn(itemDetailPk);
                       outboundService.updareLocateCnt(-1, lpn.getLocateLpnCode());
                       outboundService.deleteOutBItemDetailTB(itemDetailPk);
                   }
               }
           }
          }catch (Exception e){
              outboundService.setSqlSessionRollback();
              e.printStackTrace();
          }finally {
              outboundService.setSqlSessionCommit(flag);

              return outbLogicList;
          }
    }

}
