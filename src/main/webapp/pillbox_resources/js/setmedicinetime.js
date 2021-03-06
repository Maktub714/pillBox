$(document).ready(function(){

    $("#submit").click(function() {

        var obj = $("input[name='times_dose_times']");
        if (obj.val().trim() == '') {
            tips(obj, "(服药时间(剂量)不能为空)");
            return;
        }

        $('#loadingToast').show();
        $("#setTime").submit();
    });

    function tips(obj, text) {
        $(obj).parent(".form-group").addClass("has-error");
        $(obj).parent(".form-group").find(".control-label").find("small").text(text);
    }

    $("input").focus(function() {
        $(this).parent(".form-group").removeClass("has-error");
        $(this).parent(".form-group").find(".control-label").find("small").text("");
    })

    $("select[name=gap]").change(function () {

        var text = $(this).val();
        if ("2" == text) {
            $(".row").eq(2).css("display", "none");
        }else {
            $(".row").eq(2).css("display", "block");
        }
    });


    $("input[name=times_dose_times]").click(function(){
        $("#setTime").attr("action", "/pillBox/medicine/setDoseTime");
        $("#setTime").submit();
    });

});