package com.example.walkingdragon;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MountainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_mountain, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //배경 ImageView 찾기
        ImageView backgroundImage = view.findViewById(R.id.backgroundImage);
        AnimationDrawable animationDrawable = (AnimationDrawable) backgroundImage.getBackground();

        //캐릭터 ImageView 찾기
        ImageView characterImage = view.findViewById(R.id.characterAnimation);
        AnimationDrawable animationCharacter = (AnimationDrawable) characterImage.getDrawable();

        //ObjectAnimator 설정
        if (backgroundImage != null) {
            // 이미지의 전체 너비 계산 (혹은 고정 크기)
            animationDrawable.start();
            animationCharacter.start();
        } else {
            // backgroundImage가 null일 경우 디버깅용 로그 추가
            Log.e("MountainFragment", "backgroundImage is null");
        }
    }
}
