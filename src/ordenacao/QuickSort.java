package ordenacao;

public class QuickSort {
    public static void ordenar(int[] arr) {
        if (arr == null || arr.length < 2) return;
        quickSort(arr, 0, arr.length - 1);
    }

    private static void quickSort(int[] arr, int baixo, int alto) {
        if (baixo < alto) {
            int pi = particionar(arr, baixo, alto);
            quickSort(arr, baixo, pi - 1);
            quickSort(arr, pi + 1, alto);
        }
    }

    private static int particionar(int[] arr, int baixo, int alto) {
        int pivo = arr[alto];
        int i = baixo - 1;

        for (int j = baixo; j < alto; j++) {
            if (arr[j] <= pivo) {
                i++;
                trocar(arr, i, j);
            }
        }
        trocar(arr, i + 1, alto);
        return i + 1;
    }

    private static void trocar(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}