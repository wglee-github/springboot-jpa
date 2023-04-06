package jpabook.jpashop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.ItemDTO;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.web.BookForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;
	
	@GetMapping("/items/new")
	public String createForm(Model model) {
		model.addAttribute("form", new BookForm());
		return "items/createItemForm";
	}
	
	@PostMapping("/items/new")
	public String create(BookForm bookForm) {
		
		Book book = new Book();
		book.setName(bookForm.getName());
		book.setPrice(bookForm.getPrice());
		book.setStockQuantity(bookForm.getStockQuantity());
		book.setAuthor(bookForm.getAuthor());
		book.setIsbn(bookForm.getIsbn());
		
		log.info("book : {}", book.getName());
		
		itemService.saveItem(book);
		
		return "redirect:/items";
	}
	
	@GetMapping("/items")
	public String list(Model model){
		List<Item> items = itemService.findItems();
		model.addAttribute("items", items);
		return "/items/itemList";
	}
	
	@GetMapping("/items/{itemId}/edit")
	public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
		Book book =  (Book)itemService.findItem(itemId);
		
		BookForm form = new BookForm();
		form.setId(book.getId());
		form.setName(book.getName());
		form.setAuthor(book.getAuthor());
		form.setPrice(book.getPrice());
		form.setStockQuantity(book.getStockQuantity());
		form.setIsbn(book.getIsbn());
		
		model.addAttribute("form", form);
		
		return "items/updateItemForm";
	}
	
	@PostMapping("/items/{itemId}/edit")
	public String updateItem(@PathVariable("itemId") Long itemId, @ModelAttribute("form") ItemDTO itemDTO) {
		
		// 엔티티에 셋팅 해서 merge 한 방법. 이런식으로 하면 안된다.
//		Book book = new Book();
//		book.setId(bookForm.getId());
//		book.setName(bookForm.getName());
//		book.setAuthor(bookForm.getAuthor());
//		book.setPrice(bookForm.getPrice());
//		book.setStockQuantity(bookForm.getStockQuantity());
//		book.setIsbn(bookForm.getIsbn());
//		
//		itemService.saveItem(book);
		
		/**
		 * 엔티티를 변경할 때는 항상 변경 감지를 사용하세요
			- 컨트롤러에서 어설프게 엔티티를 생성하지 마세요.
			- 트랜잭션이 있는 서비스 계층에 식별자( id )와 변경할 데이터를 명확하게 전달하세요.(파라미터 or dto)
			- 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하세요.
			- 트랜잭션 커밋 시점에 변경 감지가 실행됩니다.
		 */
		
		/**
		 * item 정보만 수정
		 * 파라미터 직접 각각 넘김 
		 * @ModelAttribute 에서  BookForm 으로 받아야 함.
		 */
//		itemService.updateItem1(itemId, bookForm.getName(), bookForm.getPrice(), bookForm.getStockQuantity());

		/**
		 * 변경 DTO 선언해서 BOOK 정보 까지 같이 업데이트 해주는 로직 만듬.
		 * @ModelAttribute 에서  ItemDTO 로 받음
		 */
		itemDTO.setId(itemId);
		itemService.updateItemBook(itemDTO);
		
		return "redirect:/items";
	}
}
