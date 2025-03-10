package com.aloha.freeorder.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.aloha.freeorder.domain.Cart;
import com.aloha.freeorder.domain.CartOption;

@Mapper
public interface CartMapper {
    
    // 조회
    public Cart select(String id) throws Exception;
    // 조회 유저아이디 & 상품아이디
    public List<Cart> ListByUsersIdAndProductsId(@Param("usersId") String usersId 
                                            ,@Param("productId") String productsId) throws Exception;
    // 목록
    public List<Cart> list(String usersId) throws Exception;
    // 해당 유저 장바구니 목록
    public List<Cart> listByUser(String usersId) throws Exception;
    // 등록
    public int insert(Cart cart) throws Exception;
    // 옵션 등록
    public int insertOption(CartOption cartOption) throws Exception;
    // 수정
    public int update(Cart cart) throws Exception;
    // 수량 증가
    public int updateAmount(Cart cart) throws Exception;
    // 삭제
    public int delete(String id) throws Exception;
    // 장바구니 옵션 삭제
    public int deleteOption(String id) throws Exception;
    // 결제시 해당 내역 삭제
    public int allDeleteByUserId(String usersId) throws Exception;
    // 결제시 해당 내역 장바구니 옵션 삭제
    public int allDeleteOptionByUserId(String usersId) throws Exception;

}
