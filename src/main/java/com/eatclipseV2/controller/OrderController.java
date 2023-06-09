package com.eatclipseV2.controller;

import com.eatclipseV2.common.MessageDto;
import com.eatclipseV2.common.StringConst;
import com.eatclipseV2.entity.*;
import com.eatclipseV2.exception.NotEnoughCashException;
import com.eatclipseV2.service.CartService;
import com.eatclipseV2.service.MenuService;
import com.eatclipseV2.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
@Slf4j
public class OrderController {

    private final MenuService menuService;
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/{menuId}")
    public String menuDtlByMember(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                                  @PathVariable Long menuId, Model model) {
        model.addAttribute("member", loginMember);
        Menu menu = menuService.menuDtl(menuId);
        Shop shop = menu.getShop();
        model.addAttribute("shop", shop);
        model.addAttribute("menu", menu);

        return "menus/menuDtl";
    }

    @PostMapping
    public String order(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                        @RequestParam("shopId") Long shopId, @RequestParam("id") Long menuId,
                        @RequestParam int count, Model model) {
        model.addAttribute("member", loginMember);

        Long orderId = orderService.makeOrder(loginMember.getId(), shopId, menuId, count);
        Order order = orderService.findOrderByOrderId(orderId);

        int orderPrice = order.getOrderPrice();
        int totalPrice = order.getTotalPrice();

        try {
            order.getMember().minusCash(totalPrice);
        } catch (NotEnoughCashException e) {
            int currentCash = order.getMember().getCash();
            int needCash = totalPrice - currentCash;

            MessageDto messageDto = new MessageDto("캐시가 부족합니다. 충전이 필요합니다.\n(현재 잔액 : "
                    + currentCash + "원 \n 주문 총금액(배달비 포함) : " + totalPrice + "원 \n 필요 금액 : " + needCash + "원)",
                    "/members/chargeCash", RequestMethod.GET, null);

            return showMessageAndRedirect(messageDto, model);
        }

        MessageDto messageDto = new MessageDto("주문이 되었습니다. \n식당에서 접수할 예정입니다.\n(주문 금액 : "
                + orderPrice + "원, 배달 금액 : 3000원, 총 금액 : " + totalPrice + "원)",
                "/order/member/" + orderId, RequestMethod.GET, null);

        return showMessageAndRedirect(messageDto, model);
    }

    @PostMapping("/cart")
    public String cartOrder(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                            Model model) {

        model.addAttribute("member", loginMember);

        Cart cart = cartService.findCartByMemberId(loginMember.getId());
        Long orderId = orderService.makeOrder(loginMember.getId(), cart);
        Order order = orderService.findOrderByOrderId(orderId);

        int orderPrice = order.getOrderPrice();
        int totalPrice = order.getTotalPrice();

        try {
            order.getMember().minusCash(totalPrice);
        } catch (NotEnoughCashException e) {
            int currentCash = order.getMember().getCash();
            int needCash = totalPrice - currentCash;

            MessageDto messageDto = new MessageDto("캐시가 부족합니다. 충전이 필요합니다.\n(현재 잔액 : "
                    + currentCash + "원 \n 주문 총금액(배달비 포함) : " + totalPrice + "원 \n 필요 금액 : " + needCash + "원)",
                    "/members/chargeCash", RequestMethod.GET, null);

            return showMessageAndRedirect(messageDto, model);
        }

        cartService.deleteCartMenus(loginMember.getId());
        if (cart.getCartMenus().isEmpty()) {
            cartService.deleteCart(cart);
        }

        MessageDto messageDto = new MessageDto("주문이 되었습니다. \n식당에서 접수할 예정입니다.\n(주문 금액 : "
                + orderPrice + "원, 배달 금액 : 3000원, 총 금액 : " + totalPrice + "원)",
                "/order/member/" + orderId, RequestMethod.GET, null);

        return showMessageAndRedirect(messageDto, model);
    }

    @GetMapping("/member/list")
    public String orderList(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                            Model model) {
        model.addAttribute("member", loginMember);
        List<Order> orders = orderService.orderListByMember(loginMember.getId());
        model.addAttribute("orders", orders);
        return "order/orderListByMember";
    }

    @GetMapping("/shop/list")
    public String orderList(@SessionAttribute(name = StringConst.LOGIN_SHOP) Shop loginShop,
                            Model model) {
        model.addAttribute("shop", loginShop);
        List<Order> orders = orderService.orderListByShop(loginShop.getId());
        model.addAttribute("orders", orders);

        return "main-shopLogin";
    }

    @GetMapping("/shop/{orderId}")
    public String orderDtlByShop(@SessionAttribute(name = StringConst.LOGIN_SHOP) Shop loginShop,
                                 Model model, @PathVariable Long orderId) {

        model.addAttribute("shop", loginShop);
        Order order = orderService.findOrderByOrderId(orderId);
        int orderPrice = order.getOrderPrice();
        model.addAttribute("order", order);
        model.addAttribute("orderPrice", orderPrice);

        // TODO: 2023-04-29 029 Cart 기능 시 수정 예정
        OrderMenu orderMenu = order.getOrderMenus().get(0);
        model.addAttribute("orderMenu", orderMenu);
        return "order/orderDtlByShop";
    }

    @GetMapping("/shop/{orderId}/response")
    public String shopResponse(@SessionAttribute(name = StringConst.LOGIN_SHOP) Shop loginShop,
                      Model model, @PathVariable Long orderId, @RequestParam String orderStatus) {
        model.addAttribute("shop", loginShop);
        orderService.shopResponse(orderId, orderStatus);

        return "redirect:/order/shop/list";
    }

    @GetMapping("/member/{orderId}")
    public String orderDtlByShop(@SessionAttribute(name = StringConst.LOGIN_MEMBER) Member loginMember,
                                 Model model, @PathVariable Long orderId) {

        model.addAttribute("member", loginMember);
        Order order = orderService.findOrderByOrderId(orderId);
        int orderPrice = order.getOrderPrice();
        int totalPrice = order.getTotalPrice();

        model.addAttribute("order", order);
        model.addAttribute("orderPrice", orderPrice);
        model.addAttribute("totalPrice", totalPrice);

        if (order.getOrderMenus().size() == 1) {
            OrderMenu orderMenu = order.getOrderMenus().get(0);
            model.addAttribute("orderMenu", orderMenu);
            return "order/orderDtlByMember-oneMenu";
        } else  {
            return "order/orderDtlByMember";
        }
    }

    private String showMessageAndRedirect(final MessageDto messageDto, Model model) {
        model.addAttribute("params", messageDto);
        return "common/messageRedirect";
    }
}
