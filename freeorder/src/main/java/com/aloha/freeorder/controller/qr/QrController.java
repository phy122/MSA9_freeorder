package com.aloha.freeorder.controller.qr;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aloha.freeorder.domain.Cart;
import com.aloha.freeorder.domain.CartOption;
import com.aloha.freeorder.domain.Category;
import com.aloha.freeorder.domain.Notice;
import com.aloha.freeorder.domain.Option;
import com.aloha.freeorder.domain.OptionItem;
import com.aloha.freeorder.domain.Order;
import com.aloha.freeorder.domain.OrderItem;
import com.aloha.freeorder.domain.Payment;
import com.aloha.freeorder.domain.Product;
import com.aloha.freeorder.service.CartService;
import com.aloha.freeorder.service.CategoryService;
import com.aloha.freeorder.service.NoticeService;
import com.aloha.freeorder.service.OptionService;
import com.aloha.freeorder.service.OrderService;
import com.aloha.freeorder.service.PaymentService;
import com.aloha.freeorder.service.ProductService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 페이지 컨트롤러
 * 뷰페이지 매핑
 */
@Slf4j
@Controller
@RequestMapping("/qr")
public class QrController {

  @Autowired
  private NoticeService noticeService;
  @Autowired
  private CategoryService categoryService;
  @Autowired
  private ProductService productService;
  @Autowired
  private CartService cartService;
  @Autowired
  private PaymentService paymentService;
  @Autowired
  private OrderService orderService;
  @Autowired
  private OptionService optionService;

  @GetMapping("/main")
  public String main(Model model,
      HttpServletRequest request,
      HttpServletResponse response,
      @CookieValue(value = "id", defaultValue = "null") String cookieId) {

    HttpSession session = request.getSession();
    String id = UUID.randomUUID().toString();
    log.info("생성된 아이디: " + id);
    if (!cookieId.equals("null")) {
      log.info("쿠키가 있음");
      id = cookieId;
    } else {
      log.info("쿠키가 생성..");
      Cookie cookie = new Cookie("id", id);
      cookie.setMaxAge(60 * 60 * 24 * 1);
      response.addCookie(cookie);
    }

    

    session.setAttribute("id", id);
    log.info("쿠키 아이디 : " + cookieId);
    log.info("접속시 부여된 아이디 : " + id);
    log.info("메인 서버에 접속 하셨습니다.");
    return "views/qr/main";
  }

  @GetMapping("/notice")
  public String notice(Model model) throws Exception {
    log.info("공지사항 출력!!");
    Notice notice = noticeService.read();
    model.addAttribute("notice", notice);
    return "views/qr/notice";
  }

  @GetMapping("/list")
  public String productList(@RequestParam(value = "type", required = false) String type,
      @RequestParam(value = "cate", required = false) String cate,
      Model model,
      @CookieValue(value = "orderType", defaultValue = "null") String orderType,
      HttpServletResponse response) throws Exception {
    log.info("카테고리별 상품 목록 출력!!");
    
    // 매장/포장 정보 쿠키에 저장
    if (orderType.equals("null")) {
      log.info("매장/포장 정보 쿠키에 저장");
      Cookie cookie = new Cookie("orderType", type);
      cookie.setMaxAge(60 * 60 * 24 * 1);
      response.addCookie(cookie);
    }

    List<Category> cateList = categoryService.list();
    List<Product> productList = null;
    if (cate == null)
      productList = productService.allList();

    else
      productList = productService.listByCate(cate);

    model.addAttribute("cateId", cate);
    model.addAttribute("cateList", cateList);
    model.addAttribute("productList", productList);

    // 프로모션 등록
    List<Notice> noticeList = noticeService.list();
    model.addAttribute("noticeList", noticeList);
    log.info(productList.toString());
    return "views/qr/product/list";
  }

  @GetMapping("/read/{id}")
  public String product(@PathVariable("id") String id, Model model) throws Exception {
    log.info("상품 상세페이지 출력!!");
    // 상품 정보
    Product product = productService.select(id);
    log.info("상품 정보 : " + product);
    model.addAttribute("product", product);
    String optionsId = product.getOptionsId();
    // 옵션 정보
    Option option = null;
    if (optionsId != null && !optionsId.equals("")) {
      option = optionService.read(product.getOptionsId());
      // 옵션 목록
      List<OptionItem> itemList = option.getItemList();
      model.addAttribute("itemList", itemList);
    }
    model.addAttribute("option", option);
    return "views/qr/product/read";

  }

  @GetMapping("/cart")
  public String orderList(Model model, HttpServletRequest request) throws Exception {
    log.info("장바구니 목록 출력!!");
    HttpSession session = request.getSession();
    String id = (String) session.getAttribute("id");
    log.info("Session 에 등록 된 사용자 아이디 : " + id);
    List<Cart> cartList = cartService.listByUser(id);
    log.info(cartList.toString());
    int total = 0;
    for (Cart cart : cartList) {
      List<CartOption> optionList = cart.getOptionList();
      total += cart.getPrice() * cart.getAmount();
      for (CartOption cartOption : optionList) {
        total += cartOption.getPrice();
      }
    }

    model.addAttribute("cartList", cartList);
    model.addAttribute("total", total);
    return "views/qr/product/cart";
  }

  @GetMapping("/pay/{id}")
  public String payment(@PathVariable("id") String id, Model model) throws Exception {
    log.info("결제 창 출력!!");
    Payment payment = paymentService.select(id);
    model.addAttribute("payment", payment);
    return "views/qr/pay/pay";
  }

  @GetMapping("/pay/complete/{status}")
  public String complete(@PathVariable("status") String status, Model model) throws Exception {
    model.addAttribute("status", status);
    return "views/qr/pay/complete";
  }

  @GetMapping("/order")
  public String orderTotalList(Model model, HttpServletRequest request) {
    log.info("주문 내역 목록 출력!!");
    try {
      // 세션에서 사용자 ID 가져오기
      HttpSession session = request.getSession();
      String usersId = (String) session.getAttribute("id");

      // 사용자의 주문 목록 가져오기
      List<Order> orderList = orderService.listByUsersId(usersId); // 필요시 사용자 ID로 필터링

      // 모델에 주문 목록 추가
      model.addAttribute("orderList", orderList);
    } catch (Exception e) {
      log.error("주문 목록 조회 중 오류 발생", e);
    }
    return "views/qr/order/list";
  }

  @GetMapping("/order/read/{id}")
  public String orderDetails(@PathVariable("id") String id, Model model) {
    log.info("주문 상세 조회: {}", id);
    try {
      // 주문 조회
      Order order = orderService.read(id);
      log.info("조회된 주문 데이터: {}", order);

      if (order != null) {
        List<OrderItem> itemList = order.getItemList();
        log.info("조회된 아이템 목록: {}", itemList);

        model.addAttribute("itemList", itemList);
      } else {
        log.warn("주문 ID '{}'에 해당하는 데이터가 없습니다.", id);
      }
      model.addAttribute("order", order);
    } catch (Exception e) {
      log.error("주문 상세 조회 중 에러 발생: {}", e.getMessage(), e);
    }
    return "views/qr/order/read";
  }

  @GetMapping("/option/{id}")
  public String option(@PathVariable("id") String id, Model model) throws Exception {
    log.info("옵션 리스트 출력!!");
    // 장바구니 정보 조회
    Cart cart = cartService.select(id);
    model.addAttribute("cart", cart);
    Product product = productService.select(cart.getProductsId());
    model.addAttribute("product", product);
    log.info("상품 정보 : "+product);
    // 장바구니 옵션 리스트 - optionItemsId
    List<CartOption> cartOptionList = cart.getOptionList();
    log.info("장바구니 옵션 리스트 : " + cartOptionList);
    // 상품 옵션 리스트 - id
    if (product.getOption() != null && product.getOption().getItemList() != null) {
      List<OptionItem> optionList = product.getOption().getItemList();
      for (OptionItem optionItem : optionList) {
        if (cartOptionList != null) {
          for (CartOption cartOption : cartOptionList) {
            if (cartOption.getOptionItemsId().equals(optionItem.getId())) {
              optionItem.setChecked(true);
            }
          }
        }
      }
      log.info(optionList.toString());
      model.addAttribute("optionList", optionList);
    } else {
      model.addAttribute("optionList", Collections.emptyList());
    }

    return "views/qr/product/option";
  }
}
