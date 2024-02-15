package com.av.srchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.*;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatwindo extends AppCompatActivity {
    String reciverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNName;
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;
    public static String senderImg;
    public static String reciverIImg;
    CardView sendbtn;
    EditText textmsg;

    String senderRoom, reciverRoom;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatwindo);
        Objects.requireNonNull(getSupportActionBar()).hide();
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        reciverName = getIntent().getStringExtra("nameeee");
        reciverimg = getIntent().getStringExtra("reciverImg");
        reciverUid = getIntent().getStringExtra("uid");

        messagesArrayList = new ArrayList<>();

        sendbtn = findViewById(R.id.sendbtnn);
        textmsg = findViewById(R.id.textmsg);
        reciverNName = findViewById(R.id.recivername);
        profile = findViewById(R.id.profileimgg);
        messageAdpter = findViewById(R.id.msgadpter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdpter.setLayoutManager(linearLayoutManager);
        mmessagesAdpter = new messagesAdpter(chatwindo.this, messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);


        Picasso.get().load(reciverimg).into(profile);
        reciverNName.setText("" + reciverName);

        SenderUID = firebaseAuth.getUid();

        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;


        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");


        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                    String modifiedMessage = LongStringToBlocks.dec(messages.getMessage(), "1111111111111111111111111111111111111111111111111111111111111111");
                    messages.setMessage(modifiedMessage);
                    messagesArrayList.add(messages);
                }
                mmessagesAdpter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderImg = snapshot.child("profilepic").getValue().toString();
                reciverIImg = reciverimg;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = LongStringToBlocks.enc(textmsg.getText().toString(), "1111111111111111111111111111111111111111111111111111111111111111");
                ;
                if (message.isEmpty()) {
                    Toast.makeText(chatwindo.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                    return;
                }
                textmsg.setText("");
                Date date = new Date();
                msgModelclass messagess = new msgModelclass(message, SenderUID, date.getTime());

                database = FirebaseDatabase.getInstance();
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push().setValue(messagess).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                database.getReference().child("chats")
                                        .child(reciverRoom)
                                        .child("messages")
                                        .push().setValue(messagess).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                            }
                        });
            }
        });

    }
}

class LongStringToBlocks {
    // Метод перевода строки в длинную систему с основанием 2^16
    public static String toLongSystem(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            StringBuilder binary = new StringBuilder(Integer.toBinaryString(c));
            while (binary.length() < 16) {
                binary.insert(0, "0");
            }
            sb.append(binary);
        }
        return sb.toString();
    }

    // Метод разделения строки на блоки по 64 бита
    public static List<String> divideTo64BitBlocks(String s) {
        List<String> blocks = new ArrayList<>();
        for (int i = 0; i < s.length(); i += 64) {
            StringBuilder block = new StringBuilder(s.substring(i, Math.min(i + 64, s.length())));
            while (block.length() < 64) {
                block.append("0");
            }
            blocks.add(block.toString());
        }
        return blocks;
    }

    // Метод разделения каждого блока на 4 блока по 16 бит
    public static List<String> divideTo16BitBlocks(String block) {
        List<String> blocks = new ArrayList<>();
        for (int i = 0; i < block.length(); i += 16) {
            StringBuilder block16Bit = new StringBuilder(block.substring(i, Math.min(i + 16, block.length())));
            blocks.add(block16Bit.toString());
        }
        return blocks;
    }

    // Начальная перестановка
    public static List<String> permute(List<String> inputs) {
        int[] perm = {30, 48, 15, 49, 2, 62, 39, 31, 25, 14, 59, 19, 22, 54, 18, 28, 50, 4, 32, 45, 60, 17, 61, 9, 36, 8, 51, 56, 5, 27, 20, 1, 58, 24, 10, 16, 37, 38, 55, 11, 13, 23, 43, 41, 57, 46, 33, 64, 7, 21, 47, 6, 44, 12, 42, 26, 40, 63, 34, 53, 29, 52, 3, 35};
        List<String> result = new ArrayList<>();

        for (String input : inputs) {
            StringBuilder sb = new StringBuilder();
            for (int j : perm) {
                sb.append(input.charAt(j - 1));
            }
            result.add(sb.toString());
        }

        return result;
    }

    // Генерация случайного ключа
    public static String generateKey() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // Генерируем 64 бит случайных чисел (48 * 2 = 128)
        for (int i = 0; i < 64; i++) {
            int bit = random.nextInt(2);
            sb.append(bit);
        }

        return sb.toString();
    }

    // Разбиение ключа на части
    public static String[] splitKey(String key, int chunkSize) {
        int length = key.length();
        int numOfChunks = length / chunkSize;

        String[] parts = new String[numOfChunks];

        for (int i = 0; i < numOfChunks; i++) {
            int startIndex = i * chunkSize;
            int endIndex = startIndex + chunkSize;
            parts[i] = key.substring(startIndex, endIndex);
        }

        return parts;
    }

    public static List<String> extendTo32Bit(List<String> numbers16Bit) {
        List<String> numbers32Bit = new ArrayList<>();
        for (String number : numbers16Bit) {
            StringBuilder extendedNumber = new StringBuilder();
            char signBit = number.charAt(0); // Можно также использовать number.substring(0, 1)
            for (int i = 0; i <= 16; i++) {
                extendedNumber.append(signBit);
            }
            extendedNumber.append(number.substring(1));
            numbers32Bit.add(extendedNumber.toString());
        }
        return numbers32Bit;
    }

    public static List<String> performXorOperation(List<String> list1, List<String> list2) {
        List<String> result = new ArrayList<>();

        // XOR между 1 из первого списка и 1-м элементом из второго списка
        String xor1tmp = xorStrings(list1.get(0), list2.get(0));

        // XOR между результатом xor1tmp и 2-м элементом из второго списка
        String xor2tmp = xorStrings(xor1tmp, list1.get(1));

        // XOR между 4 из первого списка и 1-м элементом из второго списка
        String xor3 = xorStrings(xor2tmp, list2.get(1));

        // XOR между 1 из первого списка и 1-м элементом из второго списка
        String xor4tmp = xorStrings(list1.get(3), list2.get(0));

        // XOR между результатом xor1tmp и 2-м элементом из второго списка
        String xor5tmp = xorStrings(xor4tmp, list1.get(2));

        // XOR между 4 из первого списка и 1-м элементом из второго списка
        String xor6 = xorStrings(xor5tmp, list2.get(1));


        result.add(xor3);
        result.add(xor6);

        return result;
    }

    public static String xorStrings(String s1, String s2) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s1.length(); i++) {
            // Получаем i-ый бит из первой строки
            char c1 = s1.charAt(i);

            // Получаем i-ый бит из второй строки
            char c2 = s2.charAt(i);

            // Выполняем операцию XOR для i-ых битов и добавляем результат в строку-результат
            char xor = (c1 != c2) ? '1' : '0';

            result.append(xor);
        }

        return result.toString();
    }

    private static final int[][] S_BOXES = {
            // S1
            {
                    14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
                    0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
                    4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
                    15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13
            },
            // S2
            {
                    15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
                    3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
                    0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
                    13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9
            }

    };

    public static String encrypt(String input) {
        StringBuilder output = new StringBuilder();
        int index = 0;

        while (index < input.length()) {
            // Получение блока
            String block = input.substring(index, index + 4);
            index += 4;

            // Преобразование из двоичного в десятичное число
            int decimal = Integer.parseInt(block, 2);

            // Получение значений строки и столбца
            int row = (decimal >> 1) & 0x03;
            int column = ((decimal & 0x01) << 3) | ((decimal & 0x10) >> 4);

            // Получение значения из S-блока
            int sBoxValue = S_BOXES[0][row * 16 + column];

            // Преобразование из десятичного в двоичное число
            String binary = Integer.toBinaryString(sBoxValue);
            binary = ("0000" + binary).substring(binary.length());

            // Добавление в выходную строку
            output.append(binary);
        }

        return output.toString();
    }

    private static final int[] P_TABLE = {
            16, 7, 20, 21, 29, 12, 28, 17,
            1, 15, 23, 26, 5, 18, 31, 10,
            2, 8, 24, 14, 32, 27, 3, 9,
            19, 13, 30, 6, 22, 11, 4, 25
    };

    public static String encryptP(String input) {
        StringBuilder output = new StringBuilder();
        for (int j : P_TABLE) {
            output.append(input.charAt(j - 1));
        }
        return output.toString();
    }

    public static String decryptP(String input) {
        StringBuilder output = new StringBuilder();
        int[] inverseTable = new int[32];

        // Создаем обратную таблицу
        for (int i = 0; i < P_TABLE.length; i++) {
            inverseTable[P_TABLE[i] - 1] = i + 1;
        }

        // Выполняем обратное преобразование
        for (int j : inverseTable) {
            output.append(input.charAt(j - 1));
        }

        return output.toString();
    }


    public static String decrypt(String input) {
        StringBuilder output = new StringBuilder();
        int index = 0;
        while (index < input.length()) {
            // Получение блока
            String block = input.substring(index, index + 4);
            index += 4;
            // Преобразование из двоичного в десятичное число
            int decimal = Integer.parseInt(block, 2);
            // Получение значений строки и столбца
            int row = (decimal >> 1) & 0x03;
            int column = ((decimal & 0x01) << 3) | ((decimal & 0x10) >> 4);
            // Получение значения из обратного S-блока
            int sBoxValue = S_BOXES[1][row * 16 + column]; // Используется S-бокс с индексом 1
            // Преобразование из десятичного в двоичное число
            String binary = Integer.toBinaryString(sBoxValue);
            binary = ("0000" + binary).substring(binary.length());
            // Добавление в выходную строку
            output.append(binary);
        }

        return output.toString();
    }

    public static List<String> performInverseXorOperation(List<String> list1, List<String> list2) {
        List<String> result = new ArrayList<>();

        String xor1tmp = xorInverseStrings(list1.get(0), list2.get(0));
        String xor2tmp = xorInverseStrings(xor1tmp, list1.get(1));
        String xor3 = xorInverseStrings(xor2tmp, list2.get(1));

        String xor4tmp = xorInverseStrings(list1.get(3), list2.get(0));
        String xor5tmp = xorInverseStrings(xor4tmp, list1.get(2));
        String xor6 = xorInverseStrings(xor5tmp, list2.get(1));

        result.add(xor3);
        result.add(xor6);

        return result;
    }

    public static String xorInverseStrings(String s1, String s2) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s1.length(); i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);

            char xor = (c1 == c2) ? '0' : '1';  // Инвертируем операцию XOR
            result.append(xor);
        }

        return result.toString();
    }

    public static List<String> truncateTo16Bit(List<String> numbers32Bit) {
        List<String> numbers16Bit = new ArrayList<>();
        for (String number : numbers32Bit) {
            numbers16Bit.add(number.substring(16));
        }
        return numbers16Bit;
    }


    private static int convertToOriginal(String block) {
        int originalBlock = 0;
        int index = 0;

        for (int i = 0; i < block.length(); i++) {
            if (block.charAt(i) == '1') {
                originalBlock |= (1 << index);
            }
            index++;
        }

        return originalBlock;
    }

    public static List<String> reversePermute(List<String> inputs) {
        int[] perm = {30, 48, 15, 49, 2, 62, 39, 31, 25, 14, 59, 19, 22, 54, 18, 28, 50, 4, 32, 45, 60, 17, 61, 9, 36, 8, 51, 56, 5, 27, 20, 1, 58, 24, 10, 16, 37, 38, 55, 11, 13, 23, 43, 41, 57, 46, 33, 64, 7, 21, 47, 6, 44, 12, 42, 26, 40, 63, 34, 53, 29, 52, 3, 35};
        List<String> result = new ArrayList<>();
        for (String input : inputs) {
            char[] chars = new char[input.length()];
            for (int i = 0; i < perm.length; i++) {
                chars[perm[i] - 1] = input.charAt(i);
            }
            result.add(String.valueOf(chars));
        }

        return result;
    }

    public static String fromLongSystem(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i += 16) {
            String binary = s.substring(i, Math.min(i + 16, s.length()));
            int decimal = Integer.parseInt(binary, 2);
            sb.append((char) decimal);
        }
        return sb.toString();
    }

    public static String join16BitBlocks(List<String> blocks) {
        StringBuilder combinedBlock = new StringBuilder();
        for (String block : blocks) {
            combinedBlock.append(block.trim());
        }
        return combinedBlock.toString();
    }

    public static List<String> removeTrailingZeros(List<String> blocks) {
        List<String> modifiedBlocks = new ArrayList<>();

        for (String block : blocks) {
            String modifiedBlock = block;
            while (modifiedBlock.endsWith("0000000000000000")) {
                modifiedBlock = modifiedBlock.substring(0, modifiedBlock.length() - 16);
            }
            modifiedBlocks.add(modifiedBlock);
        }

        return modifiedBlocks;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        String key = generateKey();
        System.out.println(dec(enc(s, key), key));

    }

    public static String enc(String s, String key) {
        String longSystem = toLongSystem(s);
        List<String> parts = Arrays.asList(splitKey(key, 32));
        List<String> blocks64BitTmp = divideTo64BitBlocks(longSystem);
        List<String> blocks64Bit = permute(blocks64BitTmp);
        List<List<String>> siphertext = new ArrayList<>();

        for (String block64Bit : blocks64Bit) {
            List<String> blocks16Bit = divideTo16BitBlocks(block64Bit);
            List<String> extendedBlocks = extendTo32Bit(blocks16Bit);
            System.out.println(extendedBlocks);
            System.out.println(extendedBlocks);
            String temp0 = extendedBlocks.get(0);
            String temp1 = extendedBlocks.get(1);
            String temp2 = extendedBlocks.get(2);
            String temp3 = extendedBlocks.get(3);
            extendedBlocks.set(0, temp2);
            extendedBlocks.set(1, temp0);
            extendedBlocks.set(2, temp3);
            extendedBlocks.set(3, temp1);
            siphertext.add(extendedBlocks);
            System.out.println(siphertext);

        }
        List<String> combinedBlocks = new ArrayList<>();
        for (List<String> block : siphertext) {
            StringBuilder combinedSubblocks = new StringBuilder();
            for (String subblock : block) {
                combinedSubblocks.append(subblock);
            }
            combinedBlocks.add(combinedSubblocks.toString());
        }
        String res = "";
        for (String blocks : combinedBlocks)
            res += blocks;
        return res;
    }

    public static String dec(String res, String key) {
        List<List<String>> ar = convertStringToArray(res);

        List<String> parts = Arrays.asList(splitKey(key, 32));
        List<String> ortext = new ArrayList<>();
        for (List<String> block : ar) {
            String temp0 = block.get(0);
            String temp1 = block.get(1);
            String temp2 = block.get(2);
            String temp3 = block.get(3);
            block.set(0, temp1);
            block.set(1, temp3);
            block.set(2, temp0);
            block.set(3, temp2);
            List<String> shrtbl = truncateTo16Bit(block);
            ortext.add(join16BitBlocks(shrtbl));
            System.out.println(ortext);


        }

        String tmpor = join16BitBlocks(removeTrailingZeros(reversePermute(ortext)));
        String originaltext = fromLongSystem(tmpor);
        return originaltext;
    }

    public static List<List<String>> convertStringToArray(String inputString) {
        List<List<String>> result = new ArrayList<>();

        // Проверяем, что длина строки кратна 128 (32 символа * 4 подблока * 4 блока)
        if (inputString.length() % 128 != 0) {
            throw new IllegalArgumentException("Неверный формат строки");
        }

        int numBlocks = inputString.length() / 128;

        // Проходим по каждому блоку
        for (int i = 0; i < numBlocks; i++) {
            List<String> block = new ArrayList<>();

            // Проходим по каждому подблоку внутри блока
            for (int j = 0; j < 4; j++) {
                int startIndex = i * 128 + (j * 32);
                int endIndex = startIndex + 32;

                // Добавляем подстроку, представляющую подблок, в текущий блок
                block.add(inputString.substring(startIndex, endIndex));
            }

            // Добавляем текущий блок в результат
            result.add(block);
        }

        return result;
    }
}
